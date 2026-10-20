package org.miaosha.miaosha.common.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 秒杀活动状态枚举
 * 0未上架，1上架，2下架，3活动已开始，4活动未开始，5活动已结束
 */
@Getter
@ApiModel("秒杀活动状态枚举")
public enum SeckillGoodsStatusEnum {

    /**
     * 0：未上架
     */
    @ApiModelProperty("0：未上架")
    NOT_PUBLISH(0, "未上架"),

    /**
     * 1：上架
     */
    @ApiModelProperty("1：上架")
    PUBLISH(1, "上架"),

    /**
     * 2：下架
     */
    @ApiModelProperty("2：下架")
    OFFLINE(2, "下架"),

    /**
     * 3：活动已开始
     */
    @ApiModelProperty("3：活动已开始")
    ACTIVITY_START(3, "活动已开始"),

    /**
     * 4：活动未开始
     */
    @ApiModelProperty("4：活动未开始")
    ACTIVITY_NOT_START(4, "活动未开始"),

    /**
     * 5：活动已结束
     */
    @ApiModelProperty("5：活动已结束")
    ACTIVITY_END(5, "活动已结束");

    @ApiModelProperty("状态编码")
    private final Integer code;
    @ApiModelProperty("状态描述")
    private final String desc;

    SeckillGoodsStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
