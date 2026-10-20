package org.miaosha.miaosha.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.enums.LocalMsgStatusEnum;
import org.miaosha.miaosha.common.exception.BusinessException;
import org.miaosha.miaosha.common.exception.enums.MqLocalMsgCodeEnum;
import org.miaosha.miaosha.common.mq.constant.MqConstants;
import org.miaosha.miaosha.common.mq.dto.SeckillStockMsgDTO;
import org.miaosha.miaosha.common.mq.producer.IMqProducer;
import org.miaosha.miaosha.entity.MqLocalMessage;
import org.miaosha.miaosha.mapper.MqLocalMessageMapper;
import org.miaosha.miaosha.service.IMqLocalMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 本地消息表服务实现类
 * 实现本地消息表记录、定时重试投递MQ消息，保证消息可靠投递
 */
@Slf4j
@Service
public class MqLocalMessageServiceImpl extends ServiceImpl<MqLocalMessageMapper, MqLocalMessage> implements IMqLocalMessageService {

    @Resource
    private IMqProducer mqProducer;
    @Resource
    private IMqLocalMessageService mqLocalMessageService;

    private static final int MAX_CONSUME_RETRY = 3;


    /**
     * 消息重试次数 +1，并且更新下次重试时间
     * 业务规则：重试次数达到3次则标记消息为失败终止；未达到上限则更新重试计数、设置下次重试时间（延后30秒）
     * @param msgId 消息唯一id
     */
    @Override
    public void increaseRetryCount(String msgId) {
        log.info("【本地消息-增加重试次数】开始执行，msgId:{}", msgId);
        LambdaQueryWrapper<MqLocalMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MqLocalMessage::getMsgId, msgId);
        MqLocalMessage msg = getOne(wrapper);
        if(msg == null){
            log.warn("【本地消息-增加重试次数】找不到消息 msgId:{}", msgId);
            return;
        }
        Integer count = msg.getRetryCount() + 1;
        if(count==MAX_CONSUME_RETRY) {
            msg.setStatus(LocalMsgStatusEnum.SEND_PERMANENT_FAIL.getCode());
            msg.setRetryCount(count);
        }else{
            msg.setRetryCount(count);
            // 下一次重试延后30秒
            msg.setNextRetryTime(LocalDateTime.now().plusSeconds(30));
            msg.setUpdateTime(LocalDateTime.now());
        }
        updateById(msg);
        log.info("【本地消息-增加重试次数】执行完成，msgId:{},当前重试次数:{}", msgId, count);
    }

    /**
     * 保存本地待投递消息
     * 业务规则：消息发送前置落库，作为消息投递兜底，定时任务扫描重试
     * @param stockMsgDTO 库存消息DTO，组装完成的业务消息载体
     * @return MqLocalMessage 保存后的本地消息记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MqLocalMessage saveLocalMessage(SeckillStockMsgDTO stockMsgDTO) {
        log.info("【本地消息-保存待投递消息】开始执行");
        // 参数校验
        if (stockMsgDTO == null) {
            log.error("【本地消息-保存待投递消息失败】消息实体为空");
            throw new BusinessException(MqLocalMsgCodeEnum.LOCAL_MSG_ENTITY_EMPTY);
        }
        MqLocalMessage localMessage = new MqLocalMessage();
        // 生成全局唯一msgId
        String msgId = UUID.randomUUID().toString();
        localMessage.setMsgId(msgId);
        localMessage.setMsgContent(JSON.toJSONString(stockMsgDTO));
        localMessage.setExchange(MqConstants.SECKILL_STOCK_EXCHANGE);
        localMessage.setRoutingKey(MqConstants.SECKILL_STOCK_ROUTING_KEY);
        localMessage.setStatus(LocalMsgStatusEnum.WAIT_SEND.getCode()); // 0待发送
        localMessage.setRetryCount(0); // 重试次数初始0
        localMessage.setNextRetryTime(LocalDateTime.now()); // 可以立刻重试
        localMessage.setCreateTime(LocalDateTime.now());
        localMessage.setUpdateTime(LocalDateTime.now());
        mqLocalMessageService.save(localMessage);
        log.info("【本地消息-保存待投递消息】执行完成，msgId:{}", msgId);
        return localMessage;
    }

    /**
     * 查询待重试消息
     * 业务规则：查询状态为发送失败、到达重试时间、未超过最大重试次数的消息，限制单次查询数量，防止一次性加载大量数据压库
     * @param limit 单次查询条数，防止一次性加载大量数据压库
     * @param maxRetry 最大重试次数
     * @return 符合条件的本地消息列表
     */
    @Override
    public List<MqLocalMessage> listWaitSendMsg(int limit, int maxRetry) {
        log.info("【本地消息-查询待重试消息】开始执行，limit:{},maxRetry:{}", limit, maxRetry);
        LambdaQueryWrapper<MqLocalMessage> wrapper = new LambdaQueryWrapper<>();
        // 状态：2发送失败
        wrapper.eq(MqLocalMessage::getStatus, LocalMsgStatusEnum.SEND_FAIL);
        // 到达下次重试时间才允许重试（next_retry_time字段，避免频繁重试）
        wrapper.le(MqLocalMessage::getNextRetryTime, LocalDateTime.now());
        // 未达到传入的最大重试次数
        wrapper.lt(MqLocalMessage::getRetryCount, maxRetry);
        wrapper.last("LIMIT " + limit);
        List<MqLocalMessage> msgList = list(wrapper);
        log.info("【本地消息-查询待重试消息】执行完成，查询到待重试消息数量：{}", msgList.size());
        return msgList;
    }

    /**
     * 重试发送消息，发送成功更新状态；发送失败则增加重试次数，更新下次重试时间
     * 业务规则：反序列化消息内容，调用生产者发送MQ；发送成功更新状态；捕获异常自动增加重试计数
     * @param mqLocalMessage 本地消息记录
     * @return true投递成功，false投递失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retrySend(MqLocalMessage mqLocalMessage){
        log.info("【本地消息-重试投递消息】开始执行，msgId:{}", mqLocalMessage.getMsgId());
        try {
            // 把json字符串反序列化为库存消息DTO
            SeckillStockMsgDTO stockMsgDTO;
            try {
                stockMsgDTO = JSON.parseObject(mqLocalMessage.getMsgContent(), SeckillStockMsgDTO.class);
            } catch (Exception e) {
                log.error("【本地消息-重试投递失败】本地消息JSON反序列化失败 msgId:{}", mqLocalMessage.getMsgId(), e);
                throw new BusinessException(MqLocalMsgCodeEnum.MSG_CONTENT_DESERIALIZE_ERROR);
            }
            // 调用你已经实现好的发送库存消息方法
            mqProducer.sendStockMsg(stockMsgDTO);
            // 发送成功，状态改为已发送
            mqLocalMessage.setStatus(LocalMsgStatusEnum.SEND_SUCCESS.getCode());
            mqLocalMessage.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(mqLocalMessage);
            log.info("【本地消息-重试投递消息】投递成功，msgId:{}", mqLocalMessage.getMsgId());
            return true;
        } catch (BusinessException e){
            throw e;
        } catch (Exception e) {
            log.error("【本地消息-重试投递消息】投递发生异常，msgId:{}", mqLocalMessage.getMsgId(), e);
            increaseRetryCount(mqLocalMessage.getMsgId());
            return false;
        }
    }

}
