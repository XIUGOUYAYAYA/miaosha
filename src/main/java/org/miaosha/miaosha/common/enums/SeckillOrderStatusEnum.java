package org.miaosha.miaosha.common.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 秒杀订单状态枚举
 * 0：待支付
 * 1：已支付
 * 2：超时取消
 * 3：手动取消
 */
@Getter
@ApiModel("秒杀订单状态枚举")
public enum SeckillOrderStatusEnum {

    /**
     * 0：待支付
     */
    @ApiModelProperty("0：待支付")
    WAIT_PAY(0, "待支付"),

    /**
     * 1：已支付
     */
    @ApiModelProperty("1：已支付")
    PAID(1, "已支付"),

    /**
     * 2：超时取消
     */
    @ApiModelProperty("2：超时取消")
    TIME_OUT_CANCEL(2, "超时取消"),

    /**
     * 3：手动取消
     */
    @ApiModelProperty("3：手动取消")
    MANUAL_CANCEL(3, "手动取消");

    @ApiModelProperty("状态编码")
    private final Integer code;
    @ApiModelProperty("状态描述")
    private final String desc;

    SeckillOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
