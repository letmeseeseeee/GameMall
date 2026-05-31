package com.gamemall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.gamemall")
@SpringBootApplication
public class GameMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameMallApplication.class, args);
    }
}
