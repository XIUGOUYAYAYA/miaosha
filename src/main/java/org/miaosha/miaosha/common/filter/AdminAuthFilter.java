package org.miaosha.miaosha.common.filter;

import org.miaosha.miaosha.common.context.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AdminAuthFilter implements Filter {

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    // 需要管理员权限的接口列表
    private static final List<String> ADMIN_API_LIST = Arrays.asList(
            "/user/page",
            "/user/add",
            "/user/update",
            "/user/resetPwd",
            "/user/*/delete",
            "/user/dashboard/stat",
            "/goods/add",
            "/goods/update",
            "/goods/delete",
            "/goods/page",
            "/seckillGoods/add",
            "/seckillGoods/update",
            "/seckillGoods/delete",
            "/seckillGoods/publish",
            "/seckillGoods/offline",
            "/seckillGoods/admin/page",
            "/confirmSeckillOrder/admin/page",
            "/seckillStock/admin/page"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();

        boolean needAdminAuth = false;
        for (String pattern : ADMIN_API_LIST) {
            if (antPathMatcher.match(pattern, uri)) {
                needAdminAuth = true;
                break;
            }
        }
        // 不需要管理员权限，直接放行
        if (!needAdminAuth) {
            filterChain.doFilter(request, response);
            return;
        }

        // 需要管理员接口，校验角色
        Byte role = UserContext.getRole();
        // role ==1 代表管理员
        if (role == null || role != 1) {
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"msg\":\"无管理员权限\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

