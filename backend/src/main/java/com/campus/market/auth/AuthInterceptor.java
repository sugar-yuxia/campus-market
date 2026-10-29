package com.campus.market.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录鉴权：解析 Bearer Token，公开接口允许匿名访问，其余接口必须携带合法 Token。
 */
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID_ATTR = "currentUserId";

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        Long userId = token == null ? null : jwtUtil.parseUserId(token);
        if (userId != null) {
            request.setAttribute(USER_ID_ATTR, userId);
        }

        if (isPublic(request)) {
            return true;
        }
        if (userId != null) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\",\"data\":null}");
        return false;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    /**
     * 商品浏览、分类查询以及登录注册接口允许匿名访问。
     */
    private boolean isPublic(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/")) {
            return true;
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return "/api/products".equals(uri)
                || "/api/products/hot".equals(uri)
                || "/api/categories".equals(uri)
                || uri.matches("/api/products/\\d+")
                || uri.matches("/api/users/\\d+");
    }
}
