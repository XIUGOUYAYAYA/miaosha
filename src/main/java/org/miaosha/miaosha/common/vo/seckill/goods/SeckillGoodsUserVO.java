package org.miaosha.miaosha.common.vo.seckill.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("用户端秒杀商品分页出参VO")
public class SeckillGoodsUserVO {

    @ApiModelProperty("秒杀ID")
    private Long seckillGoodsId;
    // 秒杀商品名
    @ApiModelProperty("秒杀商品名")
    private String goodsName;
    // 秒杀图片地址 goods表goods_img
    @ApiModelProperty("秒杀图片地址")
    private String goodsImg;
    // 商品原价格
    @ApiModelProperty("商品原价格")
    private BigDecimal originalPrice;
    // 秒杀价格
    @ApiModelProperty("秒杀价格")
    private BigDecimal seckillPrice;

    @ApiModelProperty("剩余库存 remaining_stock")
    private Integer remainingStock;

    @ApiModelProperty("原始总库存 original_stock")
    private Integer originalStock;

    // 商品描述
    @ApiModelProperty("商品描述")
    private String goodsDesc;

    // 秒杀开始时间
    @ApiModelProperty("秒杀开始时间")
    private LocalDateTime startTime;
    // 秒杀结束时间
    @ApiModelProperty("秒杀结束时间")
    private LocalDateTime endTime;
}
