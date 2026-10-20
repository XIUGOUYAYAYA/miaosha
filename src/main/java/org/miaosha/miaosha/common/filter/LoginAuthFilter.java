package org.miaosha.miaosha.common.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.miaosha.miaosha.common.context.UserContext;
import org.miaosha.miaosha.common.context.UserContext.UserInfo;
import org.miaosha.miaosha.common.constant.RedisConstant;
import org.miaosha.miaosha.common.exception.enums.CommonErrorCodeEnum;
import org.miaosha.miaosha.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class LoginAuthFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    // 白名单：无需登录放行
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/user/login",
            "/user/register",
            "/user/captcha",
            "/seckill/preload/**",
            "/redis/test",
            "/upload/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/favicon.ico"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();

        // 白名单直接放行
        boolean matchWhite = false;
        for (String pattern : WHITE_LIST) {
            if (antPathMatcher.match(pattern, uri)) {
                matchWhite = true;
                break;
            }
        }
        if (matchWhite) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从Header拿token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            write401(response, CommonErrorCodeEnum.NOT_LOGIN.getMsg());
            return;
        }
        String token = header.substring(7);

        // 校验JWT签名、有效期
        if (!jwtUtil.validateToken(token)) {
            write401(response, CommonErrorCodeEnum.TOKEN_INVALID_OR_EXPIRE.getMsg());
            return;
        }

        Long userId;
        Byte role;
        try {
            userId = jwtUtil.getUserIdByToken(token);
            role = jwtUtil.getRoleByToken(token);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            write401(response, CommonErrorCodeEnum.TOKEN_INVALID_OR_EXPIRE.getMsg());
            return;
        }

        // 单端登录校验：Redis对比token
        String redisKey = RedisConstant.LOGIN_TOKEN_PREFIX + userId;
        String redisToken = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisToken == null || !redisToken.equals(token)) {
            write401(response, CommonErrorCodeEnum.LOGIN_EXPIRED.getMsg());
            return;
        }

        // 存入ThreadLocal上下文
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setToken(token);
        userInfo.setRole(role);
        UserContext.setUserInfo(userInfo);

        try {
            // 放行，进入后续Filter链 + Controller
            filterChain.doFilter(request, response);
        } finally {
            // finally 无论正常/异常，一定清理ThreadLocal，防止线程池复用串用户
            UserContext.clear();
        }
    }

    private void write401(HttpServletResponse response, String msg) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = "{\"code\":401,\"msg\":\"" + msg + "\"}";
        response.getWriter().write(json);
    }
}

