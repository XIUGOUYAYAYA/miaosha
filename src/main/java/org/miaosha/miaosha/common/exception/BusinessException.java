package org.miaosha.miaosha.common.exception;

import lombok.Getter;
import org.miaosha.miaosha.common.exception.enums.*;

/**
 * 业务自定义异常
 * 项目业务层面抛出的异常，统一使用该异常类，结合各模块异常枚举携带错误码与提示信息
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 业务异常编码
     */
    private final Integer code;

    /**
     * 用户模块异常构造方法
     * @param userCodeEnum 用户模块异常枚举
     */
    public BusinessException(UserCodeEnum  userCodeEnum) {
        super(userCodeEnum.getMsg());
        this.code = userCodeEnum.getCode();
    }

    /**
     * 预订单模块异常构造方法
     * @param seckillPreOrderCodeEnum 预订单模块异常枚举
     */
    public BusinessException(SeckillPreOrderCodeEnum  seckillPreOrderCodeEnum) {
        super(seckillPreOrderCodeEnum.getMsg());
        this.code = seckillPreOrderCodeEnum.getCode();
    }


    /**
     * 商品模块异常构造方法
     * @param goodsCodeEnum 商品模块异常枚举
     */
    public BusinessException(GoodsCodeEnum goodsCodeEnum) {
        super(goodsCodeEnum.getMsg());
        this.code = goodsCodeEnum.getCode();
    }

    /**
     * 本地消息模块异常构造方法
     * @param mqLocalMsgCodeEnum 本地消息模块异常枚举
     */
    public BusinessException(MqLocalMsgCodeEnum mqLocalMsgCodeEnum) {
        super(mqLocalMsgCodeEnum.getMsg());
        this.code = mqLocalMsgCodeEnum.getCode();
    }

    /**
     * 秒杀商品模块异常构造方法
     * @param seckillGoodsCodeEnum 秒杀商品模块异常枚举
     */
    public BusinessException(SeckillGoodsCodeEnum seckillGoodsCodeEnum) {
        super(seckillGoodsCodeEnum.getMsg());
        this.code = seckillGoodsCodeEnum.getCode();
    }

    /**
     * 公共通用异常构造方法
     * @param commonErrorCodeEnum 公共通用异常枚举
     */
    public BusinessException(CommonErrorCodeEnum commonErrorCodeEnum) {
        super(commonErrorCodeEnum.getMsg());
        this.code = commonErrorCodeEnum.getCode();
    }

    /**
     * 秒杀订单模块异常构造方法
     * @param orderCodeEnum 秒杀订单模块异常枚举
     */
    public BusinessException(SeckillOrderCodeEnum orderCodeEnum) {
        super(orderCodeEnum.getMsg());
        this.code = orderCodeEnum.getCode();
    }

    /**
     * 秒杀抢购记录模块异常构造方法
     * @param recordCodeEnum 秒杀抢购记录模块异常枚举
     */
    public BusinessException(SeckillRecordCodeEnum recordCodeEnum) {
        super(recordCodeEnum.getMsg());
        this.code = recordCodeEnum.getCode();
    }

    /**
     * 秒杀库存模块异常构造方法
     * @param seckillStockCodeEnum 秒杀库存模块异常枚举
     */
    public BusinessException(SeckillStockCodeEnum seckillStockCodeEnum) {
        super(seckillStockCodeEnum.getMsg());
        this.code = seckillStockCodeEnum.getCode();
    }

    /**
     * 用户收货地址模块异常构造方法
     * @param userAddressCodeEnum 用户收货地址模块异常枚举
     */
    public BusinessException(UserAddressCodeEnum userAddressCodeEnum) {
        super(userAddressCodeEnum.getMsg());
        this.code = userAddressCodeEnum.getCode();
    }


}
