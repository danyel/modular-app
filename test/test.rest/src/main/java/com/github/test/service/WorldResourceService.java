package com.github.test.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WorldResourceService {
    @Value("${test.bean.name}")
    private String beanName;

    public String hello() {
        return beanName;
    }
}
