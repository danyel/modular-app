package com.github.test.spring.config;

import com.github.test.service.LoggingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class TestWebConfiguration implements WebMvcConfigurer {
    @Bean(name = "helloService")
    LoggingService helloService() {
        return new LoggingService();
    }
}
