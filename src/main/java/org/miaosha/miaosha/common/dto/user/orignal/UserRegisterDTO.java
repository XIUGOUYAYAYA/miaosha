package org.miaosha.miaosha.common.dto.user.orignal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户注册入参DTO
 * 账号名+手机号+密码+图形验证码
 */
@Data
@ApiModel("用户注册DTO")
public class UserRegisterDTO {

    @NotBlank(message = "账号名不能为空")
    @Size(min = 3, max = 20, message = "账号名长度3~20位")
    @ApiModelProperty(value = "账号名", required = true)
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6~20位")
    @ApiModelProperty(value = "密码", required = true)
    private String password;

    @NotBlank(message = "验证码key不能为空")
    @ApiModelProperty(value = "图形验证码唯一key", required = true)
    private String captchaKey;

    @NotBlank(message = "验证码不能为空")
    @ApiModelProperty(value = "4位图形验证码", required = true)
    private String captchaCode;
}
