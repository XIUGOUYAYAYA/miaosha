package org.miaosha.miaosha.common.vo.seckill.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("用户端-秒杀订单分页列表VO（精简版）")
public class SeckillOrderUserVO {

    @ApiModelProperty("订单ID")
    private Long id;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("秒杀商品名称")
    private String goodsName;

    @ApiModelProperty("商品图片地址")
    private String goodsImg;

    // ==========新增两个字段==========
    @ApiModelProperty("商品描述")
    private String goodsDesc;

    @ApiModelProperty("剩余库存")
    private Integer remainingStock;

    @ApiModelProperty("秒杀单价")
    private BigDecimal seckillPrice;

    @ApiModelProperty("购买数量")
    private Integer count;

    @ApiModelProperty("实付金额")
    private BigDecimal payAmount;

    @ApiModelProperty("支付状态：0未支付，1已支付，2已过期，3已取消")
    private Integer payStatus;

    @ApiModelProperty("订单创建时间")
    private LocalDateTime createTime;
}
