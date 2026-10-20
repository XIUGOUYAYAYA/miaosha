package org.miaosha.miaosha.common.dto.user.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 管理员重置用户密码DTO
 */
@Data
@ApiModel("管理员重置密码DTO")
public class AdminUserResetPwdDTO {

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "待重置密码的用户ID", required = true)
    private Long userId;
}
