package org.miaosha.miaosha.common.context;

import lombok.Data;

/**
 * 用户上下文，ThreadLocal存储当前登录用户信息
 * 秒杀项目：拦截器解析token后set，接口内获取当前用户信息
 */
public class UserContext {

    /**
     * ThreadLocal 存放用户信息实体
     */
    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前登录用户信息
     * @param userInfo 用户信息
     */
    public static void setUserInfo(UserInfo userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }

    /**
     * 获取当前用户信息
     * @return UserInfo
     */
    public static UserInfo getUserInfo() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户ID
     * @return userId
     */
    public static Long getUserId() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo == null ? null : userInfo.getUserId();
    }

    /**
     * 获取当前请求的token
     * @return token
     */
    public static String getToken() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo == null ? null : userInfo.getToken();
    }

    /**
     * 获取当前用户角色
     * @return role
     */
    public static Byte getRole() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo == null ? null : userInfo.getRole();
    }

    /**
     * 【必须！】清除ThreadLocal，防止线程复用导致内存泄漏、串用户
     */
    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }

    /**
     * 内部类：存放当前登录用户信息
     */
    @Data
    public static class UserInfo {
        private Long userId;
        private String token;
        private Byte role;

    }
}
