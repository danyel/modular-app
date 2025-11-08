package com.github.test.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Setter
public class UserManagementResourceService {
    @Value("${user-management.bean.name}")
    private String beanName;

    public String hello() {
        return beanName;
    }
}
