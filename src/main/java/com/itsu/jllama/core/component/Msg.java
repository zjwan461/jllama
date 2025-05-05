package com.itsu.jllama.core.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author jerry.su
 * @date 2025/4/13 12:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Msg {

    private String title;

    private String content;

    private LocalDateTime createTime;

    private Status status;

    public enum Status {
        success,
        error,
        warning,
        info
    }
}
