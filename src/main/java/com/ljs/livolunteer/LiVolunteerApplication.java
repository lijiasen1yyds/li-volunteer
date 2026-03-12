package com.ljs.livolunteer;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ljs.livolunteer.mapper")
public class LiVolunteerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiVolunteerApplication.class, args);
    }

}
