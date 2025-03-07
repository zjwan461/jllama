package com.itsu.oa.core.filter;

import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.entity.User;
import com.itsu.oa.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    @Resource
    private UserService userService;

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;


    public static final String LOGIN_USER_KEY = "_USER";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HandlerMethod hm = null;
        try {
            hm = (HandlerMethod) requestMappingHandlerMapping.getHandler(request).getHandler();
        } catch (Exception e) {
            log.error("com.itsu.oa.core.filter.AuthFilter.doFilterInternal", e);
        }
        if (hm != null && hm.hasMethodAnnotation(Auth.class)) {
            Auth auth = hm.getMethodAnnotation(Auth.class);
            if (!auth.requireLogin()) {
                filterChain.doFilter(request, response);
            } else {
                HttpSession session = request.getSession();
                User loginUser = (User) session.getAttribute("LOGIN_USER_KEY");
                if (loginUser == null) {
                    throw new JException("登录超时或未登录");
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
