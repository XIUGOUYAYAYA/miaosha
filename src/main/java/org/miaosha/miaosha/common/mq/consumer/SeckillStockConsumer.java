package org.miaosha.miaosha.common.mq.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.mq.dto.SeckillStockMsgDTO;
import org.miaosha.miaosha.service.ISeckillStockService;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.core.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Component
public class SeckillStockConsumer {

    @Resource
    private ISeckillStockService seckillStockService;

    // 消费最大重试次数
    private static final int MAX_CONSUME_RETRY = 3;

    @RabbitListener(queues = "seckill.stock.queue")
    public void consumeStockMsg(SeckillStockMsgDTO msgBody,
                                Channel channel,
                                Message  message,
                                @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                @Header(value = "x-retry-count", required = false) Integer retryCount) throws IOException {

        Integer currentRetry = retryCount == null ? 0 : retryCount;
        log.info("收到库存MQ消息，orderNo:{}, msgType:{}，当前消费重试次数:{}",
                msgBody.getOrderNo(), msgBody.getMsgType(), currentRetry);
        boolean result;
        if (1 == msgBody.getMsgType()) {
            // 扣减数据库库存
            result = seckillStockService.deductStockInDb(
                    msgBody.getSeckillGoodsId(),
                    msgBody.getOrderNo()
            );
        } else if (2 == msgBody.getMsgType()) {
            // 归还库存
            result = seckillStockService.returnStockInDb(
                    msgBody.getSeckillGoodsId(),
                    msgBody.getOrderNo()
            );
        } else {
            log.warn("未知消息类型，直接ACK丢弃, orderNo:{}", msgBody.getOrderNo());
            channel.basicAck(deliveryTag, false);
            return;
        }

        if (result) {
            // 业务成功，ACK
            channel.basicAck(deliveryTag, false);
            log.info("库存消息ACK成功，orderNo:{}", msgBody.getOrderNo());
        } else {
            // 业务返回false，判断重试次数
            if (currentRetry < MAX_CONSUME_RETRY) {
                // 还能重试，Nack，重新放回队列
                log.warn("业务处理失败，进行重试，当前重试次数:{} orderNo:{}", currentRetry, msgBody.getOrderNo());
                int nextRetry = currentRetry + 1;
                message.getMessageProperties().getHeaders().put("x-retry-count", nextRetry);
                channel.basicNack(deliveryTag, false, true);
            } else {
                // 达到3次上限，不再重试，丢入死信（不重回队列）
                log.error("消费达到最大重试3次，转入死信队列 orderNo:{}", msgBody.getOrderNo());
                channel.basicNack(deliveryTag, false, false);
            }
        }

    }

}