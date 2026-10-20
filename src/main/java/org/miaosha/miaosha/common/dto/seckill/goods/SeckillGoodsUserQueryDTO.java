package org.miaosha.miaosha.common.dto.seckill.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@ApiModel("用户端秒杀商品分页查询DTO")
public class SeckillGoodsUserQueryDTO {
    /**
     * 页码，非必传，不传默认1
     */
    @Min(value = 1, message = "页码不能小于1")
    @ApiModelProperty("页码，非必传，不传默认1")
    private Integer pageNum;

    /**
     * 每页条数，非必传，限制最大条数，防止拖库
     */
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页最多查询20条")
    @ApiModelProperty("每页条数，非必传，限制最大条数，防止拖库")
    private Integer pageSize;

    /**
     * 商品名称模糊查询
     */
    @Size(max = 30, message = "商品名称不能超过30个字符")
    @ApiModelProperty("商品名称模糊查询")
    private String goodsName;

    /**
     * 秒杀价格下限，不能负数
     */
    @Min(value = 0, message = "最低价格不能为负数")
    @ApiModelProperty("秒杀价格下限，不能负数")
    private BigDecimal minSeckillPrice;

    /**
     * 秒杀价格上限，不能负数
     */
    @Min(value = 0, message = "最高价格不能为负数")
    @ApiModelProperty("秒杀价格上限，不能负数")
    private BigDecimal maxSeckillPrice;
}
