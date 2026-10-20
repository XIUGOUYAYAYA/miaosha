package org.miaosha.miaosha.common.mq.producer.impl;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.enums.LocalMsgStatusEnum;
import org.miaosha.miaosha.common.mq.constant.MqConstants;
import org.miaosha.miaosha.common.mq.dto.SeckillStockMsgDTO;
import org.miaosha.miaosha.common.mq.producer.IMqProducer;
import org.miaosha.miaosha.entity.MqLocalMessage;
import org.miaosha.miaosha.service.IMqLocalMessageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
public class MqProducerImpl implements IMqProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private IMqLocalMessageService mqLocalMessageService;

    // 生产者最大投递重试次数：最多重试3次
    private static final int MAX_SEND_RETRY = 3;

    /**
     * @param stockMsgDTO 库存消息封装体
     */
    @Override
    public void sendStockMsg(SeckillStockMsgDTO stockMsgDTO) {
        // 1. 先保存本地消息到数据库！【顺序不能颠倒：先落库，再发MQ】
        MqLocalMessage localMessage =mqLocalMessageService.saveLocalMessage(stockMsgDTO);
        try {
            rabbitTemplate.convertAndSend(
                    MqConstants.SECKILL_STOCK_EXCHANGE,
                    MqConstants.SECKILL_STOCK_ROUTING_KEY,
                    stockMsgDTO,
                    // MessagePostProcessor 用来修改消息，追加header
                    message -> {
                        // 首次发送，重试次数初始化为0
                        message.getMessageProperties().getHeaders().put("x-retry-count", 0);
                        return message;
                    }
            );
            // 发送成功，更新本地消息状态为【已发送】
            localMessage.setStatus(LocalMsgStatusEnum.SEND_SUCCESS.getCode());
            localMessage.setUpdateTime(LocalDateTime.now());
            mqLocalMessageService.updateById(localMessage);
            log.info("发送库存MQ消息成功，orderNo:{}, msgId:{}",
                    stockMsgDTO.getOrderNo(), localMessage.getMsgId());
        } catch (Exception e) {
            log.error("发送库存MQ消息失败！orderNo:{}", stockMsgDTO.getOrderNo(), e);
            // 发送异常，标记发送失败，等待定时任务重试
            localMessage.setStatus(LocalMsgStatusEnum.SEND_FAIL.getCode());
            localMessage.setUpdateTime(LocalDateTime.now());
            mqLocalMessageService.updateById(localMessage);
        }
    }
}

