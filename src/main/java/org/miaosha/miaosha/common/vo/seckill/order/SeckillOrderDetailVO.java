package org.miaosha.miaosha.common.vo.seckill.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("秒杀订单详情VO")
public class SeckillOrderDetailVO {

    @ApiModelProperty("订单id")
    @Min(value = 1, message = "订单id不能小于1")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNo;

    @ApiModelProperty("用户ID")
    @Min(value = 1, message = "用户ID不能小于1")
    private Long userId;

    @ApiModelProperty("秒杀商品id")
    @Min(value = 1, message = "秒杀商品id不能小于1")
    private Long seckillGoodsId;

    @ApiModelProperty("商品名称")
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

    @ApiModelProperty("订单状态：0待支付，1已支付，2超时取消，3手动取消")
    private Integer orderStatus;

    @ApiModelProperty("支付时间")
    private LocalDateTime payTime;

    @ApiModelProperty("订单过期时间")
    private LocalDateTime expireTime;

    @ApiModelProperty("订单创建时间")
    private LocalDateTime createTime;

    // ========== 新增收货地址3个字段 ==========
    @ApiModelProperty("收货人姓名")
    private String receiverName;

    @ApiModelProperty("收货人手机号")
    private String receiverPhone;

    @ApiModelProperty("收货详细地址")
    private String receiverAddress;
}
