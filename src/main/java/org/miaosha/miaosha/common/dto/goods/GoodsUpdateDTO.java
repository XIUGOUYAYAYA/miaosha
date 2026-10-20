package org.miaosha.miaosha.common.dto.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 修改商品入参
 */
@Data
@ApiModel("修改商品入参DTO")
public class GoodsUpdateDTO {

    @NotNull(message = "商品id不能为空")
    @ApiModelProperty("商品主键ID")
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @ApiModelProperty("商品名称")
    private String goodsName;

    @ApiModelProperty("商品图片地址")
    private String goodsImg;

    @NotNull(message = "商品原价不能为空")
    @Min(value = 0, message = "价格不能小于0")
    @ApiModelProperty("商品原价")
    private BigDecimal originalPrice;

    @NotNull(message = "原始库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    @ApiModelProperty("原始库存")
    private Integer originalStock;

    @Size(max = 50, message = "商品描述不能超过50个字符")
    @ApiModelProperty("商品描述")
    private String goodsDesc;

}
