package org.miaosha.miaosha.common.dto.seckill.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel("管理员秒杀商品分页查询DTO")
public class SeckillGoodsAdminQueryDTO {
    // 页码，最小不能小于1
    @Min(value = 1, message = "页码不能小于1")
    @ApiModelProperty("页码，非必传，不传默认1")
    private Integer pageNum;


    // 每页条数，限制范围，防止前端传超大值，拖垮数据库
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页最多查询20条")
    @ApiModelProperty("每页条数，非必传，限制最大条数，防止拖库")
    private Integer pageSize;

    // 秒杀商品名称（模糊查询，关联goods表）
    @Size(max = 30, message = "商品名称不能超过30个字符")
    @ApiModelProperty("秒杀商品名称（模糊查询，关联goods表）")
    private String goodsName;

    // 秒杀活动状态
    @ApiModelProperty("秒杀活动状态")
    private Integer status;
}
