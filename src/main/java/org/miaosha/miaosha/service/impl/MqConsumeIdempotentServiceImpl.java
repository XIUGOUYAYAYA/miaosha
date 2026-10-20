package org.miaosha.miaosha.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.entity.MqConsumeIdempotent;
import org.miaosha.miaosha.mapper.MqConsumeIdempotentMapper;
import org.miaosha.miaosha.service.IMqConsumeIdempotentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * MQ消费幂等记录表 服务实现类
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-07
 */

/**
 * MQ消费幂等记录服务实现类
 * 实现MQ消费幂等记录的查询、新增逻辑，防止MQ消息重复消费
 */
@Slf4j
@Service
public class MqConsumeIdempotentServiceImpl extends ServiceImpl<MqConsumeIdempotentMapper, MqConsumeIdempotent> implements IMqConsumeIdempotentService {

    @Resource
    private MqConsumeIdempotentMapper mqConsumeIdempotentMapper;

    /**
     * 判断【订单号+业务状态】是否存在幂等记录
     * 业务规则：通过订单号+业务状态联合唯一校验，判断该业务消息是否已经消费处理完成，实现幂等控制
     * @param orderNo 订单号
     * @param bizStatus 业务状态：1扣减成功，2归还成功
     * @return true存在记录（已消费），false不存在记录（未消费）
     */
    @Override
    public boolean existsByOrderNoAndBizStatus(String orderNo, Integer bizStatus) {
        log.info("【幂等记录查询】开始执行，订单号:{},业务状态:{}", orderNo, bizStatus);
        LambdaQueryWrapper<MqConsumeIdempotent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MqConsumeIdempotent::getOrderNo, orderNo)
                .eq(MqConsumeIdempotent::getBizStatus, bizStatus);
        boolean exists = mqConsumeIdempotentMapper.exists(wrapper);
        log.info("【幂等记录查询】执行完成，订单号:{},业务状态:{},是否存在:{}", orderNo, bizStatus, exists);
        return exists;
    }

    /**
     * 新增MQ消费幂等记录
     * 业务规则：消息消费业务处理成功后插入幂等记录，后续同订单同状态消息直接跳过处理，保证幂等
     * @param orderNo 订单号
     * @param seckillGoodsId 秒杀商品id
     * @param bizStatus 业务状态 1扣减 /2归还
     */
    @Override
    public void saveIdempotentRecord(String orderNo, Long seckillGoodsId, Integer bizStatus) {
        log.info("【新增幂等记录】开始执行，订单号:{},秒杀商品id:{},业务状态:{}", orderNo, seckillGoodsId, bizStatus);
        MqConsumeIdempotent idempotent = new MqConsumeIdempotent();
        idempotent.setOrderNo(orderNo);
        idempotent.setSeckillGoodsId(seckillGoodsId);
        idempotent.setBizStatus(bizStatus);
        mqConsumeIdempotentMapper.insert(idempotent);
        log.info("【新增幂等记录】执行完成，订单号:{},业务状态:{}", orderNo, bizStatus);
    }
}
