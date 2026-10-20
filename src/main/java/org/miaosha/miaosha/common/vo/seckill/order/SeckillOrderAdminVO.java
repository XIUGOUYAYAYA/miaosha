package org.miaosha.miaosha.common.vo.seckill.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("管理员端-秒杀订单分页列表VO")
public class SeckillOrderAdminVO {

    @ApiModelProperty("订单id")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("秒杀商品名称")
    private String goodsName;

    @ApiModelProperty("商品原价")
    @Min(value = 0, message = "商品原价不能小于0")
    private BigDecimal originalPrice;

    @ApiModelProperty("秒杀单价")
    @Min(value = 0, message = "秒杀单价不能小于0")
    private BigDecimal seckillPrice;

    @ApiModelProperty("购买数量")
    @Min(value = 1, message = "购买数量不能小于1")
    private Integer count;

    @ApiModelProperty("实付金额")
    @Min(value = 0, message = "实付金额不能小于0")
    private BigDecimal payAmount;

    @ApiModelProperty("支付状态：0未支付，1已支付，2已取消，3已过期")
    private Integer payStatus;

    @ApiModelProperty("订单创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("支付时间")
    private LocalDateTime payTime;
}
