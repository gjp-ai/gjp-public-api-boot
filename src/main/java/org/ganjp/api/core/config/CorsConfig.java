package org.ganjp.api.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsConfig implements WebMvcConfigurer {

    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("Content-Type", "Origin", "Accept", "Range")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
