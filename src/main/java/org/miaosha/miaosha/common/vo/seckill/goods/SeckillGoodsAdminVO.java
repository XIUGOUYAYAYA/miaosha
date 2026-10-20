package org.miaosha.miaosha.common.vo.seckill.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("管理员端秒杀商品分页出参VO")
public class SeckillGoodsAdminVO {
    // 秒杀活动ID seckill_goods.id
    @ApiModelProperty("秒杀活动ID")
    private Long seckillId;
    // 秒杀商品名（来自goods表goods_name）
    @ApiModelProperty("秒杀商品名（来自goods表）")
    private String goodsName;
    // 商品原价格（goods表original_price）
    @ApiModelProperty("商品原价格")
    private BigDecimal originalPrice;
    // 秒杀价格 seckill_goods.seckill_price
    @ApiModelProperty("秒杀价格")
    private BigDecimal seckillPrice;
    // 秒杀原始库存 seckill_goods.stock_count
    @ApiModelProperty("秒杀原始库存")
    private Integer stockCount;
    // 秒杀开始时间
    @ApiModelProperty("秒杀开始时间")
    private LocalDateTime startTime;
    // 秒杀结束时间
    @ApiModelProperty("秒杀结束时间")
    private LocalDateTime endTime;
    // 秒杀活动状态
    @ApiModelProperty("秒杀活动状态")
    private Integer status;
    // 创建时间
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    // 修改时间
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;
}
