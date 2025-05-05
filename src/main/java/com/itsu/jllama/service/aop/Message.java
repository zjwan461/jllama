package com.itsu.jllama.service.aop;

import java.lang.annotation.*;

/**
 * @author jerry.su
 * @date 2025/4/13 12:20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Message {

    String title();

    String success() default "执行完成";

    String fail() default "执行失败";

}
