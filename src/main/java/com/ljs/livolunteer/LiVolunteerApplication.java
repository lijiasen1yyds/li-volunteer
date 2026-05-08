package com.ljs.livolunteer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ljs.livolunteer.mapper")
@EnableScheduling
public class LiVolunteerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiVolunteerApplication.class, args);
    }

}
