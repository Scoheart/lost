package com.community.lostandfound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.community.lostandfound.repository")
public class LostAndFoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(LostAndFoundApplication.class, args);
    }
}