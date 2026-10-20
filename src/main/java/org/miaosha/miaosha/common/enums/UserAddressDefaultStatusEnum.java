package org.miaosha.miaosha.common.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 用户收货地址 默认地址状态枚举（isDefault）
 * 0：非默认地址
 * 1：默认地址
 */
@Getter
@ApiModel("用户收货地址默认标识枚举")
public enum UserAddressDefaultStatusEnum {

    /**
     * 0：非默认地址
     */
    @ApiModelProperty("0：非默认地址")
    NOT_DEFAULT(0, "非默认地址"),

    /**
     * 1：默认地址
     */
    @ApiModelProperty("1：默认地址")
    IS_DEFAULT(1, "默认地址");

    @ApiModelProperty("状态编码")
    private final Integer code;
    @ApiModelProperty("状态描述")
    private final String desc;

    UserAddressDefaultStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
