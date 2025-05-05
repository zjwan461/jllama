package com.itsu.jllama.core.sys;

import com.itsu.jllama.core.exception.JException;

public enum Platform {

    WINDOWS("win"), MAC("macos"), LINUX("linux");

    final String abbr;

    Platform(String abbr) {
        this.abbr = abbr;
    }

    public String getAbbr() {
        return abbr;
    }

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
