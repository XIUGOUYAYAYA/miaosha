package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 公共通用异常码 4位，00xx开始
 */
@Getter
@ApiModel("公共通用异常码枚举")
public enum CommonErrorCodeEnum {

    /**
     * 0001 参数为空
     */
    @ApiModelProperty("0001：请求参数不能为空")
    PARAM_IS_NULL(0001, "请求参数不能为空"),
    /**
     * 0002 参数非法
     */
    @ApiModelProperty("0002：请求参数非法")
    PARAM_ILLEGAL(0002, "请求参数非法"),
    /**
     * 0003 未登录
     */
    @ApiModelProperty("0003：用户未登录，请先登录")
    NOT_LOGIN(0003, "用户未登录，请先登录"),
    /**
     * 0004 无权限访问
     */
    @ApiModelProperty("0004：没有访问权限")
    NO_PERMISSION(0004, "权限不足，需要管理员身份"),
    /**
     * 0005 系统内部异常
     */
    @ApiModelProperty("0005：服务器内部异常")
    SYSTEM_ERROR(0005, "服务器内部异常"),
    /**
     * 0006 token非法或过期，请重新登录
     */
    @ApiModelProperty("0006：token非法或过期，请重新登录")
    TOKEN_INVALID_OR_EXPIRE(0006, "token非法或过期，请重新登录"),
    /**
     * 0007 登录已失效，请重新登录
     */
    @ApiModelProperty("0007：登录已失效，请重新登录")
    LOGIN_EXPIRED(0007, "登录已失效，请重新登录");

    @ApiModelProperty("异常编码")
    private final Integer code;
    @ApiModelProperty("异常提示信息")
    private final String msg;

    CommonErrorCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
