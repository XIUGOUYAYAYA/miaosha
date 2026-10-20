package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 秒杀订单模块异常码 4位字符串 40xx
 */
@Getter
@ApiModel("秒杀订单模块异常码枚举")
public enum SeckillOrderCodeEnum {

    /**
     * 4001 订单不存在
     */
    @ApiModelProperty("4001：订单不存在")
    ORDER_NOT_EXIST(4001, "订单不存在"),

    /**
     * 4002 无权操作该订单（越权查看/支付/取消）
     */
    @ApiModelProperty("4002：无权操作该订单（越权查看/支付/取消）")
    NO_PERMISSION_OP_ORDER(4002, "无权操作该订单"),

    /**
     * 4003 订单状态不支持本次支付
     */
    @ApiModelProperty("4003：订单状态不支持本次支付")
    ORDER_CAN_NOT_PAY(4003, "订单状态异常，无法支付"),

    /**
     * 4004 订单状态不可取消
     */
    @ApiModelProperty("4004：订单状态不可取消")
    ORDER_CAN_NOT_CANCEL(4004, "当前订单状态不可取消"),

    /**
     * 4005 用户已经抢购过该商品，一人一单限制
     */
    @ApiModelProperty("4005：用户已经抢购过该商品，一人一单限制")
    USER_ALREADY_BUY(4005, "您已经抢购过该商品，一人一单，不可重复下单"),

    /**
     * 4006 Redis预扣库存失败，库存不足
     */
    @ApiModelProperty("4006：Redis预扣库存失败，库存不足")
    SECKILL_STOCK_NOT_ENOUGH(4006, "秒杀商品库存不足"),

    /**
     * 4007 创建订单入库失败
     */
    @ApiModelProperty("4007：创建订单入库失败")
    CREATE_ORDER_FAIL(4007, "创建订单失败"),

    /**
     * 4008 秒杀商品未上架
     */
    @ApiModelProperty("4008：秒杀商品未上架")
    SECKILL_GOODS_NOT_PUBLISH(4008, "该秒杀商品暂未上架"),

    /**
     * 4009 秒杀活动还未开始
     */
    @ApiModelProperty("4009：秒杀活动还未开始")
    SECKILL_NOT_START(4009, "秒杀活动还未开始"),

    /**
     * 4010 秒杀活动已结束
     */
    @ApiModelProperty("4010：秒杀活动已结束")
    SECKILL_ALREADY_END(4010, "秒杀活动已结束"),

    /**
     * 4011 订单ID非法
     */
    @ApiModelProperty("4011：订单ID非法")
    ORDER_ID_INVALID(4011, "订单ID非法"),

    /**
     * 4012 用户ID非法
     */
    @ApiModelProperty("4012：用户ID非法")
    USER_ID_INVALID(4012, "用户ID非法"),

    /**
     * 4013 订单号不能为空
     */
    @ApiModelProperty("4013：订单号不能为空")
    ORDER_NO_EMPTY(4013, "订单号不能为空");

    @ApiModelProperty("异常编码")
    private final Integer code;
    @ApiModelProperty("异常提示信息")
    private final String msg;

    SeckillOrderCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
