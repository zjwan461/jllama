package com.itsu.oa.controller;

import com.itsu.oa.core.model.R;
import com.itsu.oa.core.mvc.Auth;
import com.itsu.oa.domain.Server;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/server")
public class ServerController {

    @Auth
    @GetMapping("/monitor")
    public R monitor() throws Exception {
        Server server = new Server();
        server.copyTo();
        return R.success(server);
    }
}
