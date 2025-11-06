package com.github.test.spring.config;

import be.urpi.software.modular.core.rest.spring.config.CoreRestConfiguration;
import com.github.test.service.HelloService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import(value = {CoreRestConfiguration.class})
@EnableWebMvc
public class TestWebConfiguration implements WebMvcConfigurer {
    @Bean(name = "helloService")
    HelloService helloService() {
        return new HelloService();
    }
}
