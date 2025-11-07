package com.github.test.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
class WorldService {
    private final WorldResourceService worldResourceService;

    WorldService(WorldResourceService worldResourceService) {
        this.worldResourceService = worldResourceService;
    }

    @GetMapping
    String helloWorld() {
        return worldResourceService.hello();
    }
}
