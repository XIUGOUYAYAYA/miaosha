package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 秒杀抢购记录模块异常码 11xx
 */
@Getter
@ApiModel("秒杀抢购记录模块异常码枚举")
public enum SeckillRecordCodeEnum {

    /**
     * 1101 抢购记录ID非法
     */
    @ApiModelProperty("1101：抢购记录ID非法")
    RECORD_ID_INVALID(1101, "抢购记录ID非法"),

    /**
     * 1102 抢购记录状态参数非法
     */
    @ApiModelProperty("1102：抢购记录状态参数非法")
    RECORD_STATUS_INVALID(1102, "抢购记录状态参数非法"),

    /**
     * 1103 创建秒杀抢购记录失败，秒杀资格生成异常
     */
    @ApiModelProperty("1103：创建秒杀抢购记录失败，秒杀资格生成异常")
    CREATE_RECORD_FAIL(1103, "创建秒杀抢购记录失败，秒杀资格生成异常"),

    /**
     * 1104 更新抢购记录状态失败，记录不存在
     */
    @ApiModelProperty("1104：更新抢购记录状态失败，记录不存在")
    UPDATE_RECORD_FAIL(1104, "更新抢购记录状态失败，记录不存在"),

    /**
     * 1105 更每人限购一件，你已抢购该商品
     */
    @ApiModelProperty("1105：每人限购一件，你已抢购该商品")
    SECKILL_USER_ALREADY_BUY(1105, "每人限购一件，你已抢购该商品");


    @ApiModelProperty("异常编码")
    private final Integer code;
    @ApiModelProperty("异常提示信息")
    private final String msg;

    SeckillRecordCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
