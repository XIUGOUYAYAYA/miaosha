package org.miaosha.miaosha.common.mq.constant;

/**
 * RabbitMQ 常量定义类
 * 所有交换机、队列、路由Key都写在这里，避免硬编码散落各处
 */
public class MqConstants {

    // ==================== 秒杀扣库存 正常交换机/队列 ====================
    /**
     * 秒杀库存扣减：交换机
     */
    public static final String SECKILL_STOCK_EXCHANGE = "seckill.stock.exchange";

    /**
     * 秒杀库存扣减：队列
     */
    public static final String SECKILL_STOCK_QUEUE = "seckill.stock.queue";

    /**
     * 秒杀库存扣减：路由Key
     */
    public static final String SECKILL_STOCK_ROUTING_KEY = "seckill.stock.routing.key";

    // ==================== 死信交换机/队列 ====================
    /**
     * 死信交换机：所有消费失败的消息最终都会进到这里
     */
    public static final String DLX_EXCHANGE = "dlx.exchange";

    /**
     * 死信队列：存放消费多次仍然失败的消息，等待人工排查
     */
    public static final String DLX_QUEUE = "dlx.queue";

    /**
     * 死信路由Key
     */
    public static final String DLX_ROUTING_KEY = "dlx.routing.key";
}
