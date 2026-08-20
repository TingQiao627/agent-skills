package com.dt.example.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello World 应用入口
 *
 * <p>Spring Boot 启动类，启动后访问 <a href="http://localhost:8080/api/hello">/api/hello</a> 获取欢迎消息。
 */
@SpringBootApplication
public class HelloWorldApplication {

    /**
     * 应用主入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }
}