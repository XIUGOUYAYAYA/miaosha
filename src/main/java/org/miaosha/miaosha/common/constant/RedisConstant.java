package org.miaosha.miaosha.common.constant;

/**
 * Redis相关常量
 */
public class RedisConstant {
    /**
     * 登录token redis key前缀
     */
    public static final String LOGIN_TOKEN_PREFIX = "login:token:";

    /**
     * token过期时间，单位分钟，和JWT保持一致 120分钟
     */
    public static final long TOKEN_EXPIRE_MINUTE = 120;
}

