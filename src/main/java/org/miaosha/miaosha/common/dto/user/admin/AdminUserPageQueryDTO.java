package org.miaosha.miaosha.common.dto.user.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 管理员分页查询用户列表DTO
 */
@Data
@ApiModel("管理员分页查询用户DTO")
public class AdminUserPageQueryDTO {

    @ApiModelProperty(value = "账号名（模糊查询）")
    private String username;

    @ApiModelProperty(value = "手机号（模糊查询）")
    private String phone;

    @ApiModelProperty(value = "角色：0普通用户，1管理员")
    private Byte role;

    @ApiModelProperty(value = "状态：0禁用，1正常，2已注销")
    private Byte status;

    @Min(value = 1, message = "页码不能小于1")
    @ApiModelProperty("页码，非必传，不传默认1")
    private Long pageNum;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 20, message = "每页最多查询20条")
    @ApiModelProperty("每页条数，非必传，限制最大条数，防止拖库")
    private Long pageSize;
}
