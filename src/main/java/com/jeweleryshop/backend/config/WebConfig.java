package com.jeweleryshop.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ Map /images/** cho cả ảnh static và ảnh upload
        registry.addResourceHandler("/images/**")
                .addResourceLocations(
                        "classpath:/static/images/", // ảnh trong resources/static/images
                        "file:" + uploadDir // ảnh trong thư mục uploads
                )
                .setCachePeriod(3600); // cache ảnh 1 giờ

        // ✅ Nếu bạn muốn giữ map /uploads/** riêng biệt
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(3600);
    }

}
