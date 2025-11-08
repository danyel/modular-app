package com.github.test.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-management/test")
public class UserManagementService {
    private final UserManagementResourceService userManagementResourceService;

    public UserManagementService(UserManagementResourceService userManagementResourceService) {
        this.userManagementResourceService = userManagementResourceService;
    }

    @GetMapping
    public String helloWorld() {
        return userManagementResourceService.hello();
    }
}
