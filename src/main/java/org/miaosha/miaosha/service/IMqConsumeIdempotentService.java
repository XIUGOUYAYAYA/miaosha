package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.miaosha.miaosha.entity.MqConsumeIdempotent;

/**
 * <p>
 * MQ消费幂等记录表 服务类
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-07
 */
public interface IMqConsumeIdempotentService extends IService<MqConsumeIdempotent> {

    /**
     * 判断【订单号+业务状态】是否存在记录
     * @param orderNo 订单号
     * @param bizStatus 业务状态：1扣减成功，2归还成功
     * @return true存在，false不存在
     */
    boolean existsByOrderNoAndBizStatus(String orderNo, Integer bizStatus);

    /**
     * 新增幂等记录
     * @param orderNo 订单号
     * @param seckillGoodsId 秒杀商品id
     * @param bizStatus 业务状态 1扣减 /2归还
     */
    void saveIdempotentRecord(String orderNo, Long seckillGoodsId, Integer bizStatus);
}
