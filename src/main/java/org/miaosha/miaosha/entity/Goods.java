package org.miaosha.miaosha.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 基础商品表
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-24
 */
@Data
@ApiModel(value = "Goods对象", description = "基础商品表")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("商品主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("商品名称")
    private String goodsName;

    @ApiModelProperty("商品图片地址")
    private String goodsImg;

    @ApiModelProperty("商品原价")
    private BigDecimal originalPrice;

    @ApiModelProperty("原始总库存")
    private Integer originalStock;

    @ApiModelProperty("剩余可售库存")
    private Integer remainingStock;

    @ApiModelProperty("商品描述")
    private String goodsDesc;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除：0未删除，1已删除")
    private Byte isDelete;
}
