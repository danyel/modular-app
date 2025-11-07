package com.github.test.spring.config;

import com.github.test.service.HelloService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan("com.github.test.service")
class TestWebConfiguration implements WebMvcConfigurer {
    @Bean(name = "helloService")
    HelloService helloService() {
        return new HelloService();
    }
}
