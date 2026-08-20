package com.dt.example.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello World 应用启动类。
 *
 * @author dtcoder
 */
@SpringBootApplication
public class HelloApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class, args);
    }
}