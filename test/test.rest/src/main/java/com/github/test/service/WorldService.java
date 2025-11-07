package com.github.test.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
class WorldService {
    @GetMapping
    String helloWorld() {
        return "hello world";
    }
}
