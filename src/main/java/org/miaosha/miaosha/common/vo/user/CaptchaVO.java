package org.miaosha.miaosha.common.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 图形验证码返回VO
 */
@Data
@ApiModel("图形验证码VO")
public class CaptchaVO {

    @ApiModelProperty(value = "验证码唯一key，前端提交注册时带回")
    @NotBlank(message = "验证码key不能为空")
    private String captchaKey;

    @ApiModelProperty(value = "png图片base64字符串，前端直接渲染图片")
    @NotBlank(message = "图片base64不能为空")
    private String base64Img;
}
