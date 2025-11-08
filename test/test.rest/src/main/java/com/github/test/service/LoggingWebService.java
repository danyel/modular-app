package com.github.test.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/logging/test")
@RestController
class LoggingWebService {
    private final LoggingResourceService loggingResourceService;

    LoggingWebService(LoggingResourceService loggingResourceService) {
        this.loggingResourceService = loggingResourceService;
    }

    @GetMapping
    String helloWorld() {
        return loggingResourceService.hello();
    }
}
