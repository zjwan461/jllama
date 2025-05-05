package com.itsu.jllama.core.filter;

import cn.hutool.core.util.StrUtil;
import com.itsu.jllama.core.exception.AuthException;
import com.itsu.jllama.core.exception.JException;
import com.itsu.jllama.core.mvc.Auth;
import com.itsu.jllama.entity.User;
import com.itsu.jllama.service.UserService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Slf4j
@Getter
@Setter
public class AuthFilter extends OncePerRequestFilter {

    @Resource
    private UserService userService;

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Resource
    private HandlerExceptionResolver handlerExceptionResolver;

    private AntPathMatcher pathMatcher = new AntPathMatcher();

    private String ignoredPaths;

    private List<String> ignoredPathList;

    public static final String LOGIN_USER_KEY = "_USER";

    @Override
    protected void initFilterBean() throws ServletException {
        if (StrUtil.isNotBlank(ignoredPaths)) {
            this.ignoredPathList = StrUtil.split(ignoredPaths, ",");
        }
    }

    protected boolean isIgnoredPath(HttpServletRequest request) {
        if (ignoredPathList != null && !ignoredPathList.isEmpty()) {
            String path = request.getServletPath();
            for (String ignoredPath : ignoredPathList) {
                if (pathMatcher.match(ignoredPath, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isIgnoredPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            HandlerMethod hm = null;
            try {
                hm = (HandlerMethod) requestMappingHandlerMapping.getHandler(request).getHandler();
            } catch (Exception e) {
                log.warn("not found handler method on request: {}", request.getRequestURI());
            }
            if (hm != null && hm.hasMethodAnnotation(Auth.class)) {
                Auth auth = hm.getMethodAnnotation(Auth.class);
                if (!auth.requireLogin()) {
                    filterChain.doFilter(request, response);
                } else {
                    HttpSession session = request.getSession();
                    User loginUser = (User) session.getAttribute(LOGIN_USER_KEY);
                    if (loginUser == null) {
                        throw new AuthException("登录超时或未登录");
                    }
                    filterChain.doFilter(request, response);
                }
            } else {
                throw new JException("未注册的请求：" + request.getRequestURI());
            }

        } catch (Exception e) {
            resolveException(request, response, e);
        }
    }


    /**
     * 将http filter中的异常重新委托给springmvc的handlerExceptionResolver处理，走springmvc的异常处理机制
     *
     * @param request
     * @param response
     * @param e
     */
    public void resolveException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        handlerExceptionResolver.resolveException(request, response, null, e);
    }

}


