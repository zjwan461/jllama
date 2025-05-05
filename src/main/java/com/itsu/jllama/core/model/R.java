package com.itsu.jllama.core.model;

import java.util.HashMap;

public class R extends HashMap<String, Object> {

    public static R success() {
        R r = new R();
        r.put("success", true);
        return r;
    }

    public static R success(Object data) {
        R r = success();
        r.put("data", data);
        return r;
    }

    public static R fail() {
        R r = new R();
        r.put("success", false);
        return r;
    }

    public static R fail(String msg) {
        R r = fail();
        r.put("msg", msg);
        return r;
    }
}
