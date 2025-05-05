package com.itsu.jllama.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User extends BaseEntity {

    private String username;

    private String email;

    private String password;

    private String role;
}
