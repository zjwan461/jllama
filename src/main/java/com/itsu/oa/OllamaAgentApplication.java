package com.itsu.oa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.itsu.oa.mapper")
public class OllamaAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(OllamaAgentApplication.class, args);
    }

}
