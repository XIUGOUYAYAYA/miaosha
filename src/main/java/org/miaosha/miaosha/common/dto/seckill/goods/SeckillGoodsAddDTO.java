package org.miaosha.miaosha.common.dto.seckill.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("新增秒杀商品入参DTO")
public class SeckillGoodsAddDTO {
    @NotNull(message = "商品id不能为空")
    @ApiModelProperty("商品主键ID")
    private Long goodsId;

    @NotNull(message = "秒杀价格不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价格大于0")
    @ApiModelProperty("秒杀价格")
    private BigDecimal seckillPrice;

    @NotNull(message = "库存不能为空")
    @Min(value = 1, message = "库存至少为1")
    @ApiModelProperty("秒杀库存数量")
    private Integer stockCount;

    @NotNull(message = "秒杀开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("秒杀开始时间")
    private LocalDateTime startTime;

    @NotNull(message = "秒杀结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("秒杀结束时间")
    private LocalDateTime endTime;
}
