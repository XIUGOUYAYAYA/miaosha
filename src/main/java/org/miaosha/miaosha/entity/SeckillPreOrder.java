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
 * 秒杀预订单表
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-10
 */
@Data
@TableName("seckill_pre_order")
@ApiModel(value = "SeckillPreOrder对象", description = "秒杀预订单表")
public class SeckillPreOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("预订单主键 preOrderId")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("当前登录用户id")
    private Long userId;

    @ApiModelProperty("秒杀商品id")
    private Long seckillGoodsId;

    @ApiModelProperty("预订单过期时间")
    private LocalDateTime expireTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
