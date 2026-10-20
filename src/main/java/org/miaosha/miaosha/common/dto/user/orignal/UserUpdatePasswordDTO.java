package org.miaosha.miaosha.common.dto.user.orignal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户修改密码DTO
 */
@Data
@ApiModel("修改个人密码DTO")
public class UserUpdatePasswordDTO {

    @NotBlank(message = "旧密码不能为空")
    @ApiModelProperty(value = "原密码", required = true)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度6~20位")
    @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;
}
