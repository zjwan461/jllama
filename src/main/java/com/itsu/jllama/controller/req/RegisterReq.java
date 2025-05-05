package com.itsu.jllama.controller.req;

import lombok.Data;

@Data
public class RegisterReq {

    private String username;

    private String password;

    private String rePwd;

    private String email;

}
