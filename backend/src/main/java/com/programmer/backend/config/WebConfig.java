package com.programmer.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Esto mapea la URL /Img/perfiles/** a la carpeta real de tu proyecto
        registry.addResourceHandler("/Img/perfiles/**")
                .addResourceLocations("file:src/main/resources/static/Img/perfiles/");
    }
}