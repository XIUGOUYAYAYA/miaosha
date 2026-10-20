package org.miaosha.miaosha.common.dto.user.orignal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 用户自助注销账号DTO
 * 需要输入密码确认身份
 */
@Data
@ApiModel("用户自助注销账号DTO")
public class UserCancelAccountDTO {

    @NotBlank(message = "账号密码不能为空")
    @ApiModelProperty(value = "登录密码，校验身份", required = true)
    private String password;
}
