package com.server.internshipserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 高校实习过程跟踪与评价管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.server.internshipserver.mapper")
public class InternshipServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipServerApplication.class, args);
    }

}
