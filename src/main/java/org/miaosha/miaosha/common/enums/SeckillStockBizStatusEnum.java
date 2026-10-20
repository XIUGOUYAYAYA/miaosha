package org.miaosha.miaosha.common.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 库存幂等记录业务状态枚举（MQ消费幂等表 bizStatus）
 * 1：数据库库存扣减成功
 * 2：数据库库存归还成功
 */
@Getter
@ApiModel("库存幂等记录业务状态枚举")
public enum SeckillStockBizStatusEnum {

    /**
     * 1：数据库库存扣减成功
     */
    @ApiModelProperty("1：数据库库存扣减成功")
    STOCK_DEDUCT_SUCCESS(1, "数据库库存扣减成功"),

    /**
     * 2：数据库库存归还成功
     */
    @ApiModelProperty("2：数据库库存归还成功")
    STOCK_RETURN_SUCCESS(2, "数据库库存归还成功");

    @ApiModelProperty("状态编码")
    private final Integer code;
    @ApiModelProperty("状态描述")
    private final String desc;

    SeckillStockBizStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
