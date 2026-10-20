package org.miaosha.miaosha.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 秒杀订单表
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-10
 */
@Getter
@Setter
@TableName("seckill_order")
@ApiModel(value = "SeckillOrder对象", description = "秒杀订单表")
public class SeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("订单主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("订单编号（雪花算法，业务唯一）")
    private String orderNo;

    @ApiModelProperty("下单用户ID user.id")
    private Long userId;

    @ApiModelProperty("秒杀活动ID seckill_goods.id")
    private Long seckillGoodsId;

    @ApiModelProperty("订单状态：0待支付，1已支付，2超时取消，3手动取消")
    private Integer orderStatus;

    @ApiModelProperty("支付成功时间")
    private LocalDateTime payTime;

    @ApiModelProperty("订单过期时间（抢购成功后+15分钟）")
    private LocalDateTime expireTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("收货人")
    private String receiverName;

    @ApiModelProperty("收货人手机号")
    private String receiverPhone;

    @ApiModelProperty("完整收货地址")
    private String receiverAddress;

    @ApiModelProperty("关联用户收货地址id")
    private Long addressId;

    @TableLogic
    @ApiModelProperty("逻辑删除：0未删除，1已删除")
    private Byte isDelete;
}
