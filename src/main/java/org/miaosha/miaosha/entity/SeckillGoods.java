package org.miaosha.miaosha.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 秒杀活动场次表
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-22
 */
@Data
@TableName("seckill_goods")
@ApiModel(value = "SeckillGoods对象", description = "秒杀活动场次表")
public class SeckillGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("秒杀活动ID（本场秒杀唯一编号）")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("关联基础商品ID，指向goods.id")
    private Long goodsId;

    @ApiModelProperty("秒杀活动价格")
    private BigDecimal seckillPrice;

    @ApiModelProperty("秒杀库存数量（秒杀独立库存，和基础商品普通库存分开）")
    private Integer stockCount;

    @ApiModelProperty("秒杀开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("秒杀结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("活动状态：0未上架，1上架，2下架")
    private Integer status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除：0未删除，1已删除")
    private Integer isDelete;
}
