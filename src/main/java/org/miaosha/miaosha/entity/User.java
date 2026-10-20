package org.miaosha.miaosha.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author 丫丫
 * @since 2026-10-06
 */
@Data
@ApiModel(value = "User对象", description = "用户表")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("账号名，全局唯一")
    private String username;

    @ApiModelProperty("加密后的密码")
    private String password;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("头像URL地址")
    private String avatar;

    @ApiModelProperty("角色：0普通用户，1管理员")
    private Byte role;

    @ApiModelProperty("账号状态：0禁用拉黑，1正常，2已注销")
    private Byte status;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除：0未删除，1已删除")
    private Byte deleted;
}
