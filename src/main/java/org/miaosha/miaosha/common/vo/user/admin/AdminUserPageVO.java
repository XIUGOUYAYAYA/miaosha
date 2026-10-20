package org.miaosha.miaosha.common.vo.user.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员用户分页列表VO
 * 列表页精简字段，不返回头像等大字段
 */
@Data
@ApiModel("管理员用户分页列表VO")
public class AdminUserPageVO {

    @ApiModelProperty(value = "用户id")
    private Long id;

    @ApiModelProperty(value = "账号名")
    private String username;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "角色 0普通用户 1管理员")
    private Byte role;

    @ApiModelProperty(value = "账号状态：0禁用拉黑，1正常，2注销")
    private Byte status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
