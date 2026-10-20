package org.miaosha.miaosha.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 本地消息表
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-06
 */
@Getter
@Setter
@TableName("mq_local_message")
@ApiModel(value = "MqLocalMessage对象", description = "本地消息表")
public class MqLocalMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("消息唯一ID")
    private String msgId;

    @ApiModelProperty("消息JSON内容")
    private String msgContent;

    @ApiModelProperty("交换机名称")
    private String exchange;

    @ApiModelProperty("路由Key")
    private String routingKey;

    @ApiModelProperty("0待发送 1已发送 2发送失败 3永久失败")
    private Integer status;

    @ApiModelProperty("重试次数")
    private Integer retryCount;

    @ApiModelProperty("下次重试时间")
    private LocalDateTime nextRetryTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
