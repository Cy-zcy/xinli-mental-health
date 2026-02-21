package com.example.xinli.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

/**
 * DeepSeek AI API配置类
 * 配置DeepSeek API的连接参数和WebClient
 */
@Configuration
@EnableConfigurationProperties(DeepSeekProperties.class)
public class DeepSeekConfig {

    @Autowired
    private DeepSeekProperties deepSeekProperties;

    /**
     * 创建用于DeepSeek API调用的WebClient
     */
    @Bean("deepSeekWebClient")
    public WebClient deepSeekWebClient() {
        return WebClient.builder()
                .baseUrl(deepSeekProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekProperties.getApiKey())
                .defaultHeader(HttpHeaders.USER_AGENT, "XinLi-Mental-Health-Platform/1.0")
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
}
