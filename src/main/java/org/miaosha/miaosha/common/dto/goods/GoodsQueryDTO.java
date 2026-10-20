package org.miaosha.miaosha.common.dto.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 商品分页查询入参VO
 */
@Data
@ApiModel("商品分页查询入参DTO")
public class GoodsQueryDTO {

    /**
     * 商品名称，模糊查询
     */
    @ApiModelProperty("商品名称，模糊查询")
    private String goodsName;

    /**
     * 最低价格
     */
    @ApiModelProperty("最低价格")
    @Min(value = 0, message = "最低价格不能小于0")
    private Double minPrice;

    /**
     * 最高价格
     */
    @ApiModelProperty("最高价格")
    @Min(value = 0, message = "最高价格不能小于0")
    private Double maxPrice;


    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码不能小于1")
    @ApiModelProperty("页码，非必传，不传默认1")
    private Integer pageNum;

    /**
     * 每页条数
     */
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页最多查询20条")
    @ApiModelProperty("每页条数，非必传，限制最大条数，防止拖库")
    private Integer pageSize;


}
