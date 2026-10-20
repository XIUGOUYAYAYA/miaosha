package org.miaosha.miaosha.common.mq.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 秒杀扣库存消息体
 * 生产者发送到MQ的消息格式，消费者从MQ取出后反序列化成这个对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("秒杀库存MQ消息DTO")
public class SeckillStockMsgDTO implements Serializable {

    /**
     * 订单号（用于幂等控制）
     */
    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty("订单号（用于幂等控制）")
    private String orderNo;

    /**
     * 秒杀商品id
     */
    @NotNull(message = "秒杀商品id不能为空")
    @Min(value = 1, message = "秒杀商品id不能小于1")
    @ApiModelProperty("秒杀商品id")
    private Long seckillGoodsId;

    /**
     * 消息类型：1=扣减库存，2=归还库存（同一个队列可以处理两种消息）
     */
    @NotNull(message = "消息类型不能为空")
    @ApiModelProperty("消息类型：1=扣减库存，2=归还库存（同一个队列可以处理两种消息）")
    private Integer msgType;
}
