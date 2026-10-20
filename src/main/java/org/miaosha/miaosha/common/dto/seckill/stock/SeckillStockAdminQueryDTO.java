package org.miaosha.miaosha.common.dto.seckill.stock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("管理员库存分页查询DTO")
public class SeckillStockAdminQueryDTO {
    @Min(value = 1, message = "页码不能小于1")
    @ApiModelProperty("页码，非必传，不传默认1")
    private Long pageNum = 1L;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页最多查询20条")
    @ApiModelProperty("每页条数，非必传，限制最大条数，防止拖库")
    private Long pageSize = 10L;

    @ApiModelProperty(value = "秒杀商品ID（可选，不传查询全部）", example = "1")
    private Long seckillGoodsId;

    @Min(value = 0, message = "剩余库存最小值不能小于0")
    @ApiModelProperty(value = "剩余库存最小值（可选）", example = "10")
    private Integer minRemainingStock;

    @Min(value = 0, message = "剩余库存最大值不能小于0")
    @ApiModelProperty(value = "剩余库存最大值（可选）", example = "100")
    private Integer maxRemainingStock;
}
