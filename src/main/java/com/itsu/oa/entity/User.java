package com.itsu.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String role;
}
