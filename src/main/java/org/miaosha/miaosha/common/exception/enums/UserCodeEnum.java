package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 用户模块异常码 90xx
 */
@Getter
@ApiModel("用户模块异常码枚举")
public enum UserCodeEnum {

    /**
     * 9001 图形验证码错误
     */
    @ApiModelProperty("9001：图形验证码错误")
    CAPTCHA_ERROR(9001, "图形验证码错误"),

    /**
     * 9002 用户名已存在
     */
    @ApiModelProperty("9002：用户名已存在")
    USERNAME_EXIST(9002, "用户名已存在"),

    /**
     * 9003 手机号已注册
     */
    @ApiModelProperty("9003：手机号已注册")
    PHONE_EXIST(9003, "手机号已注册"),

    /**
     * 9004 用户不存在
     */
    @ApiModelProperty("9004：用户不存在")
    USER_NOT_EXIST(9004, "用户不存在"),

    /**
     * 9005 账号被禁用或已注销，禁止登录
     */
    @ApiModelProperty("9005：账号被禁用或已注销，禁止登录")
    USER_FORBIDDEN(9005, "账号被禁用或已注销，禁止登录"),

    /**
     * 9006 密码错误
     */
    @ApiModelProperty("9006：密码错误")
    PASSWORD_ERROR(9006, "密码错误"),

    /**
     * 9007 未登录，请先登录
     */
    @ApiModelProperty("9007：未登录，请先登录")
    NOT_LOGIN(9007, "未登录，请先登录"),

    /**
     * 9008 旧密码不正确
     */
    @ApiModelProperty("9008：旧密码不正确")
    OLD_PASSWORD_ERROR(9008, "旧密码不正确"),

    /**
     * 9009 图形验证码生成失败
     */
    @ApiModelProperty("9009：图形验证码生成失败")
    CAPTCHA_CREATE_FAIL(9009, "图形验证码生成失败"),

    /**
     * 9010 不能修改当前登录管理员自身角色与状态
     */
    @ApiModelProperty("9010：不能修改当前登录管理员自身角色与状态")
    CANNOT_MODIFY_SELF_ADMIN(9010, "不能修改当前登录管理员自身角色与状态"),

    /**
     * 9011 用户信息更新失败
     */
    @ApiModelProperty("9011：用户信息更新失败")
    USER_UPDATE_FAILED(9011, "用户信息更新失败");


    @ApiModelProperty("异常编码")
    private final Integer code;
    @ApiModelProperty("异常提示信息")
    private final String msg;

    UserCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
