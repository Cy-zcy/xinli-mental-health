package com.example.xinli.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置类 - 配置静态资源访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的静态资源访问
        String uploadPath = new File(fileUploadConfig.getBasePath()).getAbsolutePath() + File.separator;
        
        registry.addResourceHandler(fileUploadConfig.getUrlPrefix() + "/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
