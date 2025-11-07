package com.github.test.service;

import org.springframework.stereotype.Service;

@Service
public class WorldResourceService {
    public String hello() {
        return "hello world from the resource service";
    }
}
