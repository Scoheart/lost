package com.community.lostandfound.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 配置静态资源处理
     * 将上传文件目录映射为可访问的静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取上传目录的真实路径
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadAbsolutePath = uploadPath.toString();
        
        // 注册上传目录为静态资源，可以通过URL访问
        // 例如：http://localhost:8080/uploads/avatars/image.jpg
        registry.addResourceHandler("/" + uploadDir + "/**")
                .addResourceLocations("file:" + uploadAbsolutePath + "/");
    }
} 