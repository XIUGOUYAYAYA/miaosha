package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 用户收货地址模块异常码 80xx
 */
@Getter
@ApiModel("用户收货地址模块异常码枚举")
public enum UserAddressCodeEnum {


    /**
     * 8001 地址数量已达上限，最多8条
     */
    @ApiModelProperty("8001：地址数量已达上限，最多8条")
    ADDRESS_MAX_LIMIT(8001, "地址数量已达上限，最多添加8条收货地址"),

    /**
     * 8002 地址不存在
     */
    @ApiModelProperty("8002：收货地址不存在")
    ADDRESS_NOT_EXIST(8002, "收货地址不存在"),

    /**
     * 8003 不能操作其他用户的地址
     */
    @ApiModelProperty("8003：不能操作不属于自己的地址")
    ADDRESS_NOT_BELONG_USER(8003, "不能操作不属于自己的地址");

    @ApiModelProperty("异常编码")
    private final Integer code;
    @ApiModelProperty("异常提示信息")
    private final String msg;

    UserAddressCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
