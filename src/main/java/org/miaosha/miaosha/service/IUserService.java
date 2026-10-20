package org.miaosha.miaosha.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.miaosha.miaosha.common.dto.user.*;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserAddDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserPageQueryDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserResetPwdDTO;
import org.miaosha.miaosha.common.dto.user.admin.AdminUserUpdateDTO;
import org.miaosha.miaosha.common.dto.user.orignal.*;
import org.miaosha.miaosha.common.vo.user.CaptchaVO;
import org.miaosha.miaosha.common.vo.user.admin.AdminUserPageVO;
import org.miaosha.miaosha.common.vo.user.orignal.DashboardStatVO;
import org.miaosha.miaosha.common.vo.user.orignal.UserInfoVO;
import org.miaosha.miaosha.common.vo.user.orignal.UserLoginVO;
import org.miaosha.miaosha.entity.User;

/**
 * 用户表 服务接口
 */
public interface IUserService extends IService<User> {

    /**
     * 获取后台首页统计数据
     * @return 统计VO
     */
    DashboardStatVO getDashboardStat();

    /**
     * 获取4位图形验证码
     * @return CaptchaVO 验证码VO（key + base64图片）
     */
    CaptchaVO createCaptcha();

    /**
     * 用户注册
     * @param dto 注册入参
     */
    void register(UserRegisterDTO dto);

    /**
     * 用户登录
     * @param dto 登录账号密码
     * @return UserLoginVO 登录信息+token
     */
    UserLoginVO login(UserLoginDTO dto);

    /**
     * 获取当前登录用户信息
     * @return UserInfoVO 用户详情
     */
    UserInfoVO getCurrentUserInfo();

    /**
     * 修改个人登录密码
     * @param dto 新旧密码
     */
    void updatePassword(UserUpdatePasswordDTO dto);

    /**
     * 退出登录，清除token
     */
    void logout();

    /**
     * 用户自助注销账号
     * @param dto 密码校验
     */
    void cancelAccount(UserCancelAccountDTO dto);

    // ========== 管理员相关接口 ==========
    /**
     * 管理员分页查询用户列表
     * @param dto 分页+筛选条件
     * @return IPage<AdminUserPageVO> 分页结果
     */
    IPage<AdminUserPageVO> adminUserPage(AdminUserPageQueryDTO dto);

    /**
     * 管理员新增用户/管理员账号
     * @param dto 新增用户入参
     */
    void adminAddUser(AdminUserAddDTO dto);

    /**
     * 管理员编辑用户信息、启用/拉黑禁用账号
     * @param dto 编辑用户入参
     */
    void adminUpdateUser(AdminUserUpdateDTO dto);

    /**
     * 管理员重置用户密码
     * @param dto 待重置用户id + 新密码
     * @return 新密码
     */
    String adminResetPwd(AdminUserResetPwdDTO dto);

    /**
     * 管理员逻辑删除用户
     * @param userId 用户id
     */
    void adminDeleteUser(Long userId);
}
