package org.miaosha.miaosha.common.config;

import org.miaosha.miaosha.common.mq.constant.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置类
 * 作用：在RabbitMQ服务器上自动创建交换机、队列、绑定关系
 * 项目启动时自动执行，不需要手动去RabbitMQ控制台建
 */
@Configuration
public class RabbitMqConfig {

    // ==================== 一、死信交换机和死信队列 ====================

    /**
     * 死信交换机（Direct类型）
     * 什么是死信？消息变成"死信"的三种情况：
     * 1. 消息被消费者 reject/nack 并且 requeue=false（不重回队列）
     * 2. 消息在队列里存活时间过期（TTL）
     * 3. 队列达到最大长度，放不下新消息了
     */
    @Bean
    public DirectExchange dlxExchange() {
        // durable=true：交换机持久化，RabbitMQ重启不丢失
        return new DirectExchange(MqConstants.DLX_EXCHANGE, true, false);
    }

    /**
     * 死信队列：存放所有处理失败的消息
     * 一般不写消费者监听，人工登录RabbitMQ控制台查看、排查问题
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(MqConstants.DLX_QUEUE, true);
    }

    /**
     * 死信交换机绑定死信队列
     */
    @Bean
    public Binding dlxBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(dlxQueue)
                .to(dlxExchange)
                .with(MqConstants.DLX_ROUTING_KEY);
    }


    // ==================== 二、秒杀扣库存 正常交换机和队列 ====================

    /**
     * 秒杀库存扣减：Direct交换机
     * Direct类型：根据路由Key精确匹配，只发到匹配的队列
     */
    @Bean
    public DirectExchange seckillStockExchange() {
        return new DirectExchange(MqConstants.SECKILL_STOCK_EXCHANGE, true, false);
    }

    /**
     * 秒杀库存扣减：队列
     * 重点：设置了死信交换机！
     * 当消息被nack且不重回队列时，自动转发到死信交换机
     */
    @Bean
    public Queue seckillStockQueue() {
        // 队列参数：指定死信交换机
        Map<String, Object> args = new HashMap<>();
        // 设置死信交换机：消息消费失败后，转发到这个交换机
        args.put("x-dead-letter-exchange", MqConstants.DLX_EXCHANGE);
        // 设置死信路由Key
        args.put("x-dead-letter-routing-key", MqConstants.DLX_ROUTING_KEY);
        return new Queue(MqConstants.SECKILL_STOCK_QUEUE, true, false, false, args);
    }

    /**
     * 绑定：秒杀队列 -> 秒杀交换机，指定路由Key
     */
    @Bean
    public Binding seckillStockBinding(Queue seckillStockQueue, DirectExchange seckillStockExchange) {
        return BindingBuilder.bind(seckillStockQueue)
                .to(seckillStockExchange)
                .with(MqConstants.SECKILL_STOCK_ROUTING_KEY);
    }
}
