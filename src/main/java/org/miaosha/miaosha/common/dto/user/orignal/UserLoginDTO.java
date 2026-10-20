package org.miaosha.miaosha.common.dto.user.orignal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 用户登录DTO
 */
@Data
@ApiModel("用户登录DTO")
public class UserLoginDTO {

    @NotBlank(message = "账号名不能为空")
    @ApiModelProperty(value = "账号名", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true)
    private String password;

}
