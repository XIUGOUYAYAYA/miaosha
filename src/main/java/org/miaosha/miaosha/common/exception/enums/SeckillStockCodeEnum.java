package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 秒杀库存模块异常码 70xx
 */
@Getter
@ApiModel("秒杀库存模块异常码枚举")
public enum SeckillStockCodeEnum {

    /**
     * 7001 秒杀库存记录不存在
     */
    @ApiModelProperty("7001：秒杀库存记录不存在")
    STOCK_NOT_EXIST(7001, "秒杀库存记录不存在"),

    /**
     * 7002 Redis库存预扣失败
     */
    @ApiModelProperty("7002：Redis库存预扣失败")
    REDIS_STOCK_DEDUCT_FAIL(7002, "Redis库存预扣失败"),

    /**
     * 7003 数据库库存扣减失败，乐观锁冲突或库存不足
     */
    @ApiModelProperty("7003：数据库库存扣减失败，库存不足")
    DB_STOCK_DEDUCT_FAIL(7003, "数据库库存扣减失败，库存不足"),

    /**
     * 7004 数据库库存归还失败
     */
    @ApiModelProperty("7004：归还数据库库存失败")
    DB_STOCK_RETURN_FAIL(7004, "归还数据库库存失败"),

    /**
     * 7005 库存参数非法，库存必须大于0
     */
    @ApiModelProperty("7005：库存参数非法，库存必须大于0")
    STOCK_PARAM_INVALID(7005, "库存参数非法，库存必须大于0"),

    /**
     * 7006 库存记录创建失败
     */
    @ApiModelProperty("7006：库存记录创建失败")
    STOCK_CREATE_FAIL(7006, "库存记录创建失败"),

    /**
     * 7007 该秒杀商品库存记录已存在
     */
    @ApiModelProperty("7007：该秒杀商品库存记录已存在")
    STOCK_RECORD_EXIST(7007, "该秒杀商品库存记录已存在");


    @ApiModelProperty("异常编码")
    private final Integer code;
    @ApiModelProperty("异常提示信息")
    private final String msg;

    SeckillStockCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
