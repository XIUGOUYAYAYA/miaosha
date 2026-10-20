package org.miaosha.miaosha.common.vo.user.orignal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户登录返回VO
 */
@Data
@ApiModel("用户登录返回VO")
public class UserLoginVO {

    @ApiModelProperty(value = "登录凭证token")
    private String token;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "账号名")
    private String username;

    @ApiModelProperty(value = "角色 0普通用户 1管理员")
    private Byte role;
}
