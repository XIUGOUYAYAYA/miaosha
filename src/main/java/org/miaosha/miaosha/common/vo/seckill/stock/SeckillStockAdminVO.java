package org.miaosha.miaosha.common.vo.seckill.stock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Data
@ApiModel("管理员-秒杀库存分页VO")
public class SeckillStockAdminVO {

    @ApiModelProperty("主键id")
    @Min(value = 1, message = "主键id不能小于1")
    private Long id;

    @ApiModelProperty("秒杀商品id")
    private Long seckillGoodsId;

    @ApiModelProperty("剩余库存")
    private Integer remainingStock;

    @ApiModelProperty("原始总库存")
    private Integer originalStock;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
