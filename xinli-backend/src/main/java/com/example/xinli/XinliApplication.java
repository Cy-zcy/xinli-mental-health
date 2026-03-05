package com.example.xinli;

import com.example.xinli.config.DeepSeekProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.xinli.mapper")
@EnableConfigurationProperties(DeepSeekProperties.class)
@EnableAsync
public class XinliApplication {

    public static void main(String[] args) {
        SpringApplication.run(XinliApplication.class, args);
    }

}
