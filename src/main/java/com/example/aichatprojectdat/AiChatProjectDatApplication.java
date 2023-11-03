package com.example.aichatprojectdat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@SpringBootApplication
@EnableR2dbcAuditing
@EnableAspectJAutoProxy
public class AiChatProjectDatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatProjectDatApplication.class, args);
    }

}
