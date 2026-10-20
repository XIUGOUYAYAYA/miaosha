package org.miaosha.miaosha.common.dto.seckill.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("修改秒杀商品入参DTO")
public class SeckillGoodsUpdateDTO {
    @NotNull(message = "秒杀商品id不能为空")
    @ApiModelProperty("秒杀商品主键ID")
    private Long id;

    @ApiModelProperty("商品主键ID")
    private Long goodsId;

    @DecimalMin(value = "0.01", message = "秒杀价格大于0")
    @ApiModelProperty("秒杀价格")
    private BigDecimal seckillPrice;

    @Min(value = 1, message = "库存至少为1")
    @ApiModelProperty("秒杀库存数量")
    private Integer stockCount;

    @ApiModelProperty("秒杀开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("秒杀结束时间")
    private LocalDateTime endTime;
}
