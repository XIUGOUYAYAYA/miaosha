package org.miaosha.miaosha.common.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 秒杀抢购记录状态枚举
 * 0:已抢购(有效占用资格)  1:未抢购(资格释放/取消)
 */
@Getter
@ApiModel("秒杀抢购记录状态枚举")
public enum SeckillRecordStatusEnum {

    /**
     * 0：已抢购，有效占用资格
     */
    @ApiModelProperty("0：已抢购，有效占用资格")
    HAS_BUY(0, "已抢购，有效占用资格"),

    /**
     * 1：未抢购，资格已释放
     */
    @ApiModelProperty("1：未抢购，资格已释放")
    CANCEL(1, "未抢购，资格已释放");

    @ApiModelProperty("状态编码")
    private final Integer code;
    @ApiModelProperty("状态描述")
    private final String desc;

    SeckillRecordStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
