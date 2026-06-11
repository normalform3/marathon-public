package org.example.marathonwebapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.marathonwebapp.utils.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicPath(request)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || !Jwt.checkToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invalid or expired");
            return false;
        }

        Integer role = Jwt.parseType(token);
        if (!hasRole(request, role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Permission denied");
            return false;
        }
        return true;
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/user/login")
                || path.equals("/user/signup")
                || path.equals("/user/sendCode")
                || path.equals("/apply/submit")
                || path.equals("/race/list")
                || path.equals("/race/detail")
                || path.equals("/route/exist")
                || path.equals("/route/get")
                || path.startsWith("/news")
                || path.startsWith("/comment")
                || path.startsWith("/error");
    }

    private boolean hasRole(HttpServletRequest request, Integer role) {
        String path = request.getRequestURI();
        if (path.startsWith("/rule")) {
            return role != null && role == 3;
        }
        if (path.equals("/apply/list") || path.equals("/apply/approve") || path.equals("/apply/reject")) {
            return role != null && role == 3;
        }
        if (path.equals("/health/getAllDocs")
                || path.equals("/health/pass")
                || path.equals("/health/notPass")
                || path.equals("/health/notQualified")
                || path.equals("/health/setPending")) {
            return role != null && role == 3;
        }
        if (path.equals("/registration/draw")
                || path.equals("/grade/upload-grades")
                || path.equals("/race/add")
                || path.equals("/race/update")
                || path.equals("/route/submit")) {
            return role != null && (role == 2 || role == 3);
        }
        return role != null;
    }
}
