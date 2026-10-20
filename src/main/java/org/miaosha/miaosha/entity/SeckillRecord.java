package org.miaosha.miaosha.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 用户抢购记录表，一人一单
 * </p>
 *
 * @author 丫丫
 * @since 2026-09-24
 */
@Data
@TableName("seckill_record")
@ApiModel(value = "SeckillRecord对象", description = "用户抢购记录表，一人一单")
public class SeckillRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("抢购记录主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户id user.id")
    private Long userId;

    @ApiModelProperty("关联基础商品id goods.id")
    private Long GoodsId;

    @ApiModelProperty("关联秒杀商品id seckill_goods.id")
    private Long SeckillGoodsId;

    @ApiModelProperty("关联秒杀订单id seckill_order.id")
    private Long orderId;

    @ApiModelProperty("抢购记录状态 0:已抢购 1:未抢购")
    private Integer recordStatus;

    @ApiModelProperty("抢购记录创建时间")
    private LocalDateTime createTime;

    @TableLogic
    @ApiModelProperty("逻辑删除：0未删除，1已删除")
    private Byte isDelete;
}
