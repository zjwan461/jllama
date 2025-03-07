package com.itsu.oa.core.mvc;

import cn.hutool.core.date.DateUtil;
import com.itsu.oa.core.exception.JException;
import com.itsu.oa.core.model.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = JException.class)
    @ResponseStatus(HttpStatus.OK)
    public R handle(HttpServletRequest request, JException exception) {
        log.info("JException: {}", exception.getMessage());
        R r = R.fail(exception.getMessage());
        r.put("requestUrl", request.getContextPath());
        r.put("time", DateUtil.now());
        return r;
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleOther(HttpServletRequest request, Exception e) {
        log.error("unexpect exception happened", e);
        R r = R.fail(e.getMessage());
        r.put("requestUrl", request.getContextPath());
        r.put("time", DateUtil.now());
        return r;
    }
}
