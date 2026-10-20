package org.miaosha.miaosha.common.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 本地消息状态枚举
 */
@Getter
@ApiModel("本地消息状态枚举")
public enum LocalMsgStatusEnum
{
    /**
     * 0：待发送
     */
    @ApiModelProperty("0：待发送")
    WAIT_SEND(0, "待发送"),

    /**
     * 1：发送成功
     */
    @ApiModelProperty("1：发送成功")
    SEND_SUCCESS(1, "发送成功"),

    /**
     * 2：发送失败
     */
    @ApiModelProperty("2：发送失败，达到最大重试次数")
    SEND_FAIL(2, "发送失败"),

    /**
     * 3: 永久失败，，达到最大重试次数
     */
    @ApiModelProperty("3: 永久失败，重试耗尽")
    SEND_PERMANENT_FAIL(3,  "永久失败");



    @ApiModelProperty("状态编码")
    private final Integer code;
    @ApiModelProperty("状态描述")
    private final String desc;

    LocalMsgStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
