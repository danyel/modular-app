package com.github.test.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoggingResourceService {
    @Value("${logging.bean.name}")
    private String beanName;

    public String hello() {
        return beanName;
    }
}
