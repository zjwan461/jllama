package com.itsu.jllama.core.mvc;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auth {

    boolean requireLogin() default true;

    String[] roles() default {};

    AuthRelation rel() default AuthRelation.OR;
}
