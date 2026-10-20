package org.miaosha.miaosha.common.dto.user.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理员编辑用户信息DTO
 * 可修改账号状态：0=拉黑禁用，禁止登录、禁止参与秒杀
 */
@Data
@ApiModel("管理员编辑用户DTO")
public class AdminUserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户主键ID", required = true)
    private Long id;

    @NotBlank(message = "账号名不能为空")
    @ApiModelProperty(value = "账号名", required = true)
    private String username;

    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "角色 0普通用户 1管理员", required = true)
    private Byte role;

    @ApiModelProperty(value = "状态：0拉黑禁用，1正常，2已注销", required = true)
    private Byte status;
}
