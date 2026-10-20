package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 秒杀预订单模块异常码 4位字符串 60xx
 */
@Getter
@ApiModel("秒杀预订单模块异常码枚举")
public enum SeckillPreOrderCodeEnum {

    /**
     * 6001：预订单不存在
     */
    @ApiModelProperty("6001：预订单不存在")
    PRE_ORDER_NOT_EXIST(6001, "预订单不存在"),

    /**
     * 6002：非法操作，不是你的预订单
     */
    @ApiModelProperty("6002：非法操作，不是你的预订单")
    PRE_ORDER_ILLEGAL_USER(6002, "非法操作，不是你的预订单"),

    /**
     * 6003：预订单超时，请重新抢购
     */
    @ApiModelProperty("6003：预订单超时，请重新抢购")
    PRE_ORDER_EXPIRE(6003, "预订单超时，请重新抢购");

    /**
     * 业务状态码
     */
    private final Integer code;
    /**
     * 提示信息
     */
    private final String msg;

    SeckillPreOrderCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
