package com.anup.restaurant_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Maps the URL pattern /images/** to the images/ folder on disk.
     *
     * So when the browser requests:
     *     GET /images/restaurant_5_uuid.jpg
     * Spring looks for the file at:
     *     <project-root>/images/restaurant_5_uuid.jpg
     * and streams it back as a response.
     *
     * This is how the <img src="/images/..."> tag works on the frontend.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = "file:" + Paths.get("images/")
                .toAbsolutePath()
                .toString() + "/";

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:images/");
    }
}