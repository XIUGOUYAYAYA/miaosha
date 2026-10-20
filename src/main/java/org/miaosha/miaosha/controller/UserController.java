package org.miaosha.miaosha.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.miaosha.miaosha.common.dto.user.*;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserAddDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserPageQueryDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserResetPwdDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserUpdateDTO;
import org.miaosha.miaosha.common.dto.user.orignal.UserCancelAccountDTO;
import org.miaosha.miaosha.common.dto.user.orignal.UserLoginDTO;
import org.miaosha.miaosha.common.dto.user.orignal.UserRegisterDTO;
import org.miaosha.miaosha.common.dto.user.orignal.UserUpdatePasswordDTO;
import org.miaosha.miaosha.common.result.Result;
import org.miaosha.miaosha.common.vo.user.CaptchaVO;
import org.miaosha.miaosha.common.vo.user.*;
import org.miaosha.miaosha.common.vo.user.admin.AdminUserPageVO;
import org.miaosha.miaosha.common.vo.user.orignal.DashboardStatVO;
import org.miaosha.miaosha.common.vo.user.orignal.UserInfoVO;
import org.miaosha.miaosha.common.vo.user.orignal.UserLoginVO;
import org.miaosha.miaosha.service.IUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户模块控制器
 * 职责：接收前端HTTP请求、JSR303参数校验、调用业务Service、统一返回Result封装结果
 */
@Api(tags = "用户模块")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    // ========== 普通用户接口 ==========
    /**
     * 获取图形验证码接口
     * @return Result<CaptchaVO> 验证码VO，包含验证码key和base64图片
     */
    @ApiOperation(value = "获取4位图形验证码", notes = "生成4位数字图形验证码，返回验证码key和base64图片，存入Redis5分钟过期")
    @GetMapping("/captcha")
    public Result<CaptchaVO> getCaptcha() {
        CaptchaVO vo = userService.createCaptcha();
        return Result.success(vo);
    }

    /**
     * 用户注册接口
     * @param dto 用户注册入参JSON，@Validated开启DTO参数校验
     * @return Result<Void>
     */
    @ApiOperation(value = "用户注册（图形验证码）", notes = "提交账号密码和图形验证码完成用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /**
     * 用户登录接口
     * @param dto 用户登录入参JSON，@Validated开启DTO参数校验
     * @return Result<UserLoginVO> 登录VO，包含JWT令牌与用户信息
     */
    @ApiOperation(value = "用户登录", notes = "账号密码登录，校验成功返回JWT令牌与用户信息")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Validated @RequestBody UserLoginDTO dto) {
        UserLoginVO vo = userService.login(dto);
        return Result.success(vo);
    }

    /**
     * 获取当前登录用户信息接口
     * @return Result<UserInfoVO> 用户基础信息VO
     */
    @ApiOperation(value = "获取当前登录用户信息", notes = "从token解析userId，查询当前登录用户基础信息")
    @GetMapping("/info")
    public Result<UserInfoVO> getInfo() {
        UserInfoVO vo = userService.getCurrentUserInfo();
        return Result.success(vo);
    }

    /**
     * 修改密码接口
     * @param dto 修改密码入参JSON，@Validated开启DTO参数校验
     * @return Result<Void>
     */
    @ApiOperation(value = "修改密码", notes = "登录用户自行修改登录密码，需要校验原密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Validated @RequestBody UserUpdatePasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.success();
    }

    /**
     * 退出登录接口
     * @return Result<Void>
     */
    @ApiOperation(value = "退出登录", notes = "用户退出登录，将当前token加入黑名单，实现主动失效")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    /**
     * 自助注销账号接口
     * @param dto 注销账号入参JSON，@Validated开启DTO参数校验
     * @return Result<Void>
     */
    @ApiOperation(value = "用户自助注销账号", notes = "登录用户提交信息，自助注销账号（逻辑删除）")
    @PostMapping("/cancelAccount")
    public Result<Void> cancelAccount(@Validated @RequestBody UserCancelAccountDTO dto) {
        userService.cancelAccount(dto);
        return Result.success();
    }

    // ========== 管理员用户接口 ==========
    /**
     * 管理员分页查询用户接口
     * @param dto 分页参数+用户筛选条件，@Validated开启DTO参数校验
     * @return Result<IPage<AdminUserPageVO>> 分页结果对象
     */
    @ApiOperation(value = "管理员分页查询用户", notes = "后台管理员分页查询平台所有用户，支持条件筛选")
    @GetMapping("/page")
    public Result<IPage<AdminUserPageVO>> page(@Validated AdminUserPageQueryDTO dto) {
        IPage<AdminUserPageVO> page = userService.adminUserPage(dto);
        return Result.success(page);
    }

    /**
     * 管理员新增用户接口
     * @param dto 新增用户入参JSON，@Validated开启DTO参数校验
     * @return Result<Void>
     */
    @ApiOperation(value = "管理员新增用户/管理员账号", notes = "后台管理员新建普通用户或者管理员账号")
    @PostMapping("/add")
    public Result<Void> add(@Validated @RequestBody AdminUserAddDTO dto) {
        userService.adminAddUser(dto);
        return Result.success();
    }

    /**
     * 管理员编辑用户接口
     * @param dto 修改用户入参JSON，@Validated开启DTO参数校验
     * @return Result<Void>
     */
    @ApiOperation(value = "管理员编辑用户信息，支持启用/禁用（拉黑）", notes = "管理员修改用户资料，可启用或禁用用户账号")
    @PutMapping("/update")
    public Result<Void> update(@Validated @RequestBody AdminUserUpdateDTO dto) {
        userService.adminUpdateUser(dto);
        return Result.success();
    }

    /**
     * 管理员重置用户密码接口
     * @param dto 重置密码入参JSON，@Validated开启DTO参数校验
     * @return Result<String> 新密码
     */
    @ApiOperation(value = "管理员重置用户密码", notes = "管理员为指定用户重置密码，返回新密码")
    @PutMapping("/resetPwd")
    public Result<String> resetPwd(@Validated @RequestBody AdminUserResetPwdDTO dto) {
        String newPwd = userService.adminResetPwd(dto);
        return Result.success(newPwd);
    }

    /**
     * 管理员删除用户接口
     * @param userId 用户主键id，路径变量传递
     * @return Result<Void>
     */
    @ApiOperation(value = "管理员删除用户（逻辑删除）", notes = "管理员根据用户ID逻辑删除用户数据")
    @DeleteMapping("/{userId}")
    public Result<Void> delete(@PathVariable Long userId) {
        userService.adminDeleteUser(userId);
        return Result.success();
    }

    /**
     * 获取仪表盘统计数据接口
     * @return Result<DashboardStatVO> 首页统计数据VO
     */
    @ApiOperation(value = "获取首页仪表盘统计数据", notes = "获取后台首页仪表盘的平台统计指标")
    @GetMapping("/dashboard/stat")
    public Result<DashboardStatVO> dashboardStat() {
        DashboardStatVO statVO = userService.getDashboardStat();
        return Result.success(statVO);
    }
}
