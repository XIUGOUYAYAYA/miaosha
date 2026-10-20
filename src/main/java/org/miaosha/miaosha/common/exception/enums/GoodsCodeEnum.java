package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 商品模块业务异常码枚举
 * 编码规则：2xxx，4位数字，2代表商品模块
 */
@Getter
@ApiModel("商品模块业务异常码枚举")
public enum GoodsCodeEnum {

    /**
     * 2001：商品名称重复，新增商品时名称已存在
     */
    @ApiModelProperty("2001：商品名称重复，新增商品时名称已存在")
    GOODS_NAME_EXIST(2001, "商品名称已存在，请勿重复添加"),

    /**
     * 2002：商品不存在或已逻辑删除
     */
    @ApiModelProperty("2002：商品不存在或已逻辑删除")
    GOODS_NOT_EXIST(2002, "商品不存在或已被删除"),

    /**
     * 2003：商品关联秒杀活动，禁止删除
     */
    @ApiModelProperty("2003：商品关联秒杀活动，禁止删除")
    GOODS_CAN_NOT_DELETE_HAS_SECKILL(2003, "该商品关联秒杀活动，不允许删除"),

    /**
     * 2004：商品参数非法
     */
    @ApiModelProperty("2004：商品参数非法")
    GOODS_PARAM_INVALID(2004, "商品请求参数非法");

    /**
     * 异常编码
     */
    @ApiModelProperty("异常编码")
    private final Integer code;
    /**
     * 异常提示信息
     */
    @ApiModelProperty("异常提示信息")
    private final String msg;

    GoodsCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
