package org.miaosha.miaosha.common.vo.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * 商品分页查询出参VO
 */
@Data
@ApiModel("商品分页查询出参VO")
public class GoodsPageVO {

    /**
     * 商品主键ID
     */
    @ApiModelProperty("商品主键ID")
    private Long id;

    /**
     * 商品名称
     */
    @ApiModelProperty("商品名称")
    private String goodsName;

    /**
     * 商品图片地址
     */
    @ApiModelProperty("商品图片地址")
    private String goodsImg;

    /**
     * 商品原价
     */
    @ApiModelProperty("商品原价")
    @Min(value = 0, message = "价格不能小于0")
    private BigDecimal originalPrice;

    /**
     * 原始库存
     */
    @ApiModelProperty("原始库存")
    @Min(value = 0, message = "库存不能小于0")
    private Integer originalStock;

    /**
     * 剩余库存
     */
    @ApiModelProperty("剩余库存")
    @Min(value = 0, message = "库存不能小于0")
    private Integer remainingStock;

    /**
     * 商品描述
     */
    @ApiModelProperty("商品描述")
    private String goodsDesc;
}
