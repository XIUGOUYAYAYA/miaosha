package org.miaosha.miaosha.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * MQ消费幂等表
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-06
 */
@Data
@TableName("mq_consume_idempotent")
@ApiModel(value = "MqConsumeIdempotent对象", description = "MQ消费幂等表")
public class MqConsumeIdempotent implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("订单号，幂等唯一键")
    private String orderNo;

    @ApiModelProperty("秒杀商品ID")
    private Long seckillGoodsId;

    @ApiModelProperty("业务状态码：1扣减库存，2归还库存")
    private Integer bizStatus;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
