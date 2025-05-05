package com.itsu.jllama.core.mvc;

import cn.hutool.core.date.DateUtil;
import com.itsu.jllama.core.exception.AuthException;
import com.itsu.jllama.core.exception.JException;
import com.itsu.jllama.core.model.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static R getResp(String e, HttpServletRequest request) {
        R r = R.fail(e);
        r.put("requestUrl", request.getServletPath());
        r.put("time", DateUtil.now());
        return r;
    }

    @ExceptionHandler(value = JException.class)
    @ResponseStatus(HttpStatus.OK)
    public R handle(HttpServletRequest request, JException e) {
        log.info("JException: {}", e.getMessage());
        return getResp(e.getMessage(), request);
    }

    @ExceptionHandler(value = AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R handleAuth(HttpServletRequest request, AuthException e) {
        log.info("AuthException: {}", e.getMessage());
        return getResp(e.getMessage(), request);
    }


    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleOther(HttpServletRequest request, Exception e) {
        log.error("unexpect exception happened", e);
        return getResp(e.getMessage(), request);
    }
}
