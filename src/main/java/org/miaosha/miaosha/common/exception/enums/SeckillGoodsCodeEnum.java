package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 秒杀商品模块异常码 3xxx
 */
@Getter
@ApiModel("秒杀商品模块异常码枚举")
public enum SeckillGoodsCodeEnum {

    /**
     * 3001：秒杀商品不存在
     */
    @ApiModelProperty("3001：秒杀活动不存在")
    SECKILL_GOODS_NOT_EXIST(3001, "秒杀活动不存在"),

    /**
     * 3002：秒杀价格必须小于商品原价
     */
    @ApiModelProperty("3002：秒杀价格必须小于商品原价")
    PRICE_MUST_LESS_THAN_ORIGIN(3002, "秒杀价格必须小于商品原价"),

    /**
     * 3003：秒杀库存超过基础商品剩余库存
     */
    @ApiModelProperty("3003：秒杀库存不能大于基础商品剩余库存")
    SECKILL_STOCK_OVER_BASE(3003, "秒杀库存不能大于基础商品剩余库存"),

    /**
     * 3004：秒杀开始时间不能早于当前时间
     */
    @ApiModelProperty("3004：秒杀开始时间不能早于当前时间")
    START_TIME_CANNOT_BEFORE_NOW(3004, "秒杀开始时间不能早于当前时间"),

    /**
     * 3005：秒杀结束时间不能早于开始时间
     */
    @ApiModelProperty("3005：秒杀结束时间不能早于秒杀开始时间")
    END_TIME_CANNOT_BEFORE_START(3005, "秒杀结束时间不能早于秒杀开始时间"),

    /**
     * 3006："不在秒杀活动时间"
     */
    @ApiModelProperty("3006：不在秒杀活动时间内")
    SECKILL_TIME_INVALID(3006, "不在秒杀活动时间内"),
    /**
     * 3007：该秒杀活动已有订单，不允许删除
     */
    @ApiModelProperty("3007：秒杀活动已存在订单，禁止删除")
    HAS_ORDER_CAN_NOT_DELETE(3007, "秒杀活动已存在订单，禁止删除"),

    /**
     * 3008：秒杀活动已发布，不可重复上架
     */
    @ApiModelProperty("3008：秒杀活动已经上架，请勿重复发布")
    ALREADY_PUBLISH(3008, "秒杀活动已经上架，请勿重复发布"),

    /**
     * 3009：秒杀商品添加失败
     */
    @ApiModelProperty("3009：秒杀商品添加失败")
    GOODS_SAVE_FAIL(3009, "秒杀商品添加失败"),


    /**
     * 3010：未上架状态，无法执行下架操作
     */
    @ApiModelProperty("3010：秒杀活动未上架，不能执行下架操作")
    NOT_ONLINE_CANNOT_OFFLINE(3010, "秒杀活动未上架，不能执行下架操作"),

    /**
     * 3011：秒杀结算，基础商品库存扣减失败
     */
    @ApiModelProperty("3011：商品库存扣减失败")
    STOCK_DEDUCT_FAIL(3011, "秒杀结算，基础商品库存扣减失败"),

    /**
     * 3012：已下架秒杀活动，不允许重新上架
     */
    @ApiModelProperty("3012：已下架秒杀活动，不允许重新上架")
    OFFLINE_CANNOT_REPUBLISH(3012, "该秒杀活动已下架，不可重新上架"),

    /**
     * 3013：秒杀活动已结束，不允许上架
     */
    @ApiModelProperty("3013：秒杀活动已结束，不允许上架")
    END_TIME_CANNOT_PUBLISH(3013, "秒杀活动已结束，不允许上架");


    @ApiModelProperty("异常编码")
    private final Integer code;
    @ApiModelProperty("异常提示信息")
    private final String msg;

    SeckillGoodsCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
