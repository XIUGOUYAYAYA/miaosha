package org.miaosha.miaosha.common.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * MQ库存消息类型枚举
 * 消息类型：1=扣减库存，2=归还库存
 */
@Getter
@ApiModel("MQ库存消息类型枚举")
public enum MqMsgTypeEnum {

    /**
     * 1：扣减库存
     */
    @ApiModelProperty("1：扣减库存")
    STOCK_DEDUCT(1, "扣减库存"),

    /**
     * 2：归还库存
     */
    @ApiModelProperty("2：归还库存")
    STOCK_RETURN(2, "归还库存");

    @ApiModelProperty("类型编码")
    private final Integer code;
    @ApiModelProperty("类型描述")
    private final String desc;

    MqMsgTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
