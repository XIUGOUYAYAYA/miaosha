package org.miaosha.miaosha.common.exception.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * 本地消息模块异常码枚举 5xxx
 */
@Getter
@ApiModel("本地消息模块异常码枚举")
public enum MqLocalMsgCodeEnum {

    /**
     * 5001：本地消息实体不能为空
     */
    @ApiModelProperty("5001：本地消息实体不能为空")
    LOCAL_MSG_ENTITY_EMPTY(5001, "本地消息实体不能为空"),

    /**
     * 5002：消息内容JSON序列化失败
     */
    @ApiModelProperty("5002：消息内容JSON序列化失败")
    MSG_CONTENT_SERIALIZE_ERROR(5002, "消息内容JSON序列化失败"),

    /**
     * 5003：消息内容JSON反序列化失败
     */
    @ApiModelProperty("5003：消息内容JSON反序列化失败")
    MSG_CONTENT_DESERIALIZE_ERROR(5003, "消息内容JSON反序列化失败"),

    /**
     * 5004：消息不存在
     */
    @ApiModelProperty("5004：消息不存在")
    LOCAL_MSG_NOT_EXIST(5004, "本地消息记录不存在"),

    /**
     * 5005：消息状态非法，不可重试
     */
    @ApiModelProperty("5005：消息状态非法，不可重试")
    LOCAL_MSG_STATUS_INVALID(5005, "消息状态非法，不可重试"),

    /**
     * 5006：消息已达到最大重试次数，停止重试
     */
    @ApiModelProperty("5006：消息已达到最大重试次数，停止重试")
    LOCAL_MSG_MAX_RETRY(5006, "消息已达到最大重试次数，停止重试");

    @ApiModelProperty("异常编码")
    private final Integer code;
    @ApiModelProperty("异常提示信息")
    private final String msg;

    MqLocalMsgCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
