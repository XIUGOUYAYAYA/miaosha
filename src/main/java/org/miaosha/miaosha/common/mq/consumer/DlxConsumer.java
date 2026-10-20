package org.miaosha.miaosha.common.mq.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.miaosha.miaosha.common.mq.dto.SeckillStockMsgDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 死信队列消费者
 * 作用：监听死信队列，拿到处理失败的消息，打印日志/发告警/人工修复
 * 实际项目中一般不写业务逻辑，就是记录日志+报警
 */
@Slf4j
@Component
public class DlxConsumer {

    @RabbitListener(queues = "dlx.queue")
    public void consumeDlx(SeckillStockMsgDTO msgBody,
                           Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        // 死信队列的消息：已经重试多次仍然失败，直接记录日志，人工介入
        log.error("【死信告警】收到死信消息，需要人工排查！消息内容：{}", msgBody);

        // 死信队列直接ACK，不再重试了
        channel.basicAck(deliveryTag, false);
    }
}
