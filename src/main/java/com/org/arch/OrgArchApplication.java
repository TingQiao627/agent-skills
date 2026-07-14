package com.org.arch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 组织架构管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.org.arch.mapper")
public class OrgArchApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrgArchApplication.class, args);
    }
}