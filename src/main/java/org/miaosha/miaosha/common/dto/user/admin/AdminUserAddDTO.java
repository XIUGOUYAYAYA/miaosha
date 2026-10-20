package org.miaosha.miaosha.common.dto.user.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理员新增用户/管理员账号DTO
 */
@Data
@ApiModel("管理员新增用户DTO")
public class AdminUserAddDTO {

    @NotBlank(message = "账号名不能为空")
    @ApiModelProperty(value = "账号名", required = true)
    private String username;

    @NotBlank(message = "手机号不能为空")
    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "初始密码", required = true)
    private String password;

    @NotNull(message = "账号状态不能为空")
    @ApiModelProperty(value = "账号状态 0禁用 1正常", required = true)
    private Byte status;

    @ApiModelProperty(value = "头像地址")
    private String avatar;
}
