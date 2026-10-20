package org.miaosha.miaosha.common.vo.user.orignal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 * 【个人中心查看自己、管理员查看用户详情共用】
 */
@Data
@ApiModel("用户信息VO")
public class UserInfoVO {

    @ApiModelProperty(value = "用户id")
    private Long id;

    @ApiModelProperty(value = "账号名")
    private String username;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "角色 0普通用户，1管理员")
    private Byte role;

    @ApiModelProperty(value = "账号状态 0禁用，1正常，2已注销")
    private Byte status;
}
