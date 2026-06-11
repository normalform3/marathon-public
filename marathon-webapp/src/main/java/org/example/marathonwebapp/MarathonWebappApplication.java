package org.example.marathonwebapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@EnableScheduling // 启用定时任务
@SpringBootApplication(scanBasePackages = "org.example")
@MapperScan("org.example.marathondal.mapper")
public class MarathonWebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarathonWebappApplication.class, args);
    }

}
