package com.itsu.oa.core.sys;

import com.itsu.oa.core.exception.JException;

public enum Platform {

    WINDOWS, MAC, LINUX;


    public static Platform match(String string) {
        if (string != null && !string.isEmpty()) {
            string = string.toUpperCase();
            if (string.contains(WINDOWS.name())) {
                return WINDOWS;
            } else if (string.contains(MAC.name())) {
                return MAC;
            } else if (string.contains(LINUX.name())) {
                return LINUX;
            }
        }

        throw new JException("未知的系统平台：" + string);
    }
}
