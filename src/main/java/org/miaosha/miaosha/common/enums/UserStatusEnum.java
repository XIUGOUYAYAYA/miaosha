package org.miaosha.miaosha.common.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 用户账号状态枚举（user表 status 字段）
 * 0：账号禁用拉黑
 * 1：账号正常
 * 2：账号已注销
 */
@Getter
@ApiModel("用户账号状态枚举")
public enum UserStatusEnum {

    /**
     * 0：普通用户
     */
    @ApiModelProperty("0：普通用户")
    ORDINARY_USER((byte)0, "普通用户"),

    /**
     * 1：管理员
     */
    @ApiModelProperty("1：管理员")
    ADMIN((byte)1, "管理员"),

    /**
     * 0：账号禁用拉黑
     */
    @ApiModelProperty("0：账号禁用拉黑")
    USER_FORBIDDEN((byte)0, "账号禁用拉黑"),

    /**
     * 1：账号正常
     */
    @ApiModelProperty("1：账号正常")
    USER_NORMAL((byte)1, "账号正常"),

    /**
     * 2：账号已注销
     */
    @ApiModelProperty("2：账号已注销")
    USER_CANCEL((byte)2, "账号已注销");

    @ApiModelProperty("状态编码")
    private final Byte code;
    @ApiModelProperty("状态描述")
    private final String desc;

    UserStatusEnum(Byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
