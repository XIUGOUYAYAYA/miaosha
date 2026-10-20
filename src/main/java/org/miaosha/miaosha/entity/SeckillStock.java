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
 * 秒杀库存表
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-10
 */
@Data
@TableName("seckill_stock")
@ApiModel(value = "SeckillStock对象", description = "秒杀库存表")
public class SeckillStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("库存主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("关联秒杀活动id seckill_goods.id，一场秒杀一条库存")
    private Long seckillGoodsId;

    @ApiModelProperty("剩余可秒杀库存")
    private Integer remainingStock;

    @ApiModelProperty("本场秒杀原始总库存，用来统计")
    private Integer originalStock;

    @ApiModelProperty("乐观锁版本号，每次库存扣减+1")
    private Integer version;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除：0未删除，1已删除")
    private Byte isDelete;
}
