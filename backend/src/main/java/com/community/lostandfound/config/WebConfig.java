package com.community.lostandfound.config;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    // 生产环境中静态文件的绝对路径前缀
    private static final String[] STATIC_PATH_PATTERNS = {
        "/home/laf/be/uploads/**"
    };
    
    /**
     * 配置静态资源处理
     * 将上传文件目录映射为可访问的静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取上传目录的真实路径
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadAbsolutePath = uploadPath.toString();
        
        log.info("配置静态资源映射: /{}{} -> {}", uploadDir, "/**", uploadAbsolutePath);
        
        // 注册上传目录为静态资源，可以通过URL访问
        // 例如：http://localhost:8080/uploads/avatars/image.jpg
        registry.addResourceHandler("/" + uploadDir + "/**")
                .addResourceLocations("file:" + uploadAbsolutePath + "/");
        
        // 配置特定生产环境路径的访问，例如 /home/laf/be/uploads/ 目录
        // 处理类似 http://121.40.52.9/api/home/laf/be/uploads/avatars/xxx.png 这样的URL
        for (String pathPattern : STATIC_PATH_PATTERNS) {
            log.info("配置额外静态资源映射: {} -> file:{}", pathPattern, pathPattern.replaceFirst("/home/", "/"));
            
            registry.addResourceHandler(pathPattern)
                    .addResourceLocations("file:" + pathPattern.replaceFirst("/home/", "/"));
        }
    }
} 