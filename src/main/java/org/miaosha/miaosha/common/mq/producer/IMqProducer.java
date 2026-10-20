package org.miaosha.miaosha.common.mq.producer;

import org.miaosha.miaosha.common.mq.dto.SeckillStockMsgDTO;

/**
 * MQ消息生产者接口
 * 负责发送秒杀相关消息
 */
public interface IMqProducer {

    /**
     * 发送库存操作消息（扣减库存 / 归还库存共用这个方法，由DTO内部msgType区分）
     * @param stockMsgDTO 库存消息封装体
     */
    void sendStockMsg(SeckillStockMsgDTO stockMsgDTO);
}
