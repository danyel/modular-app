package com.github.test.service;

import be.urpi.software.modular.core.service.RestService;

public class LoggingService implements RestService<String, String> {
    public String doOnGet(String uniqueIdentifier) {
        return uniqueIdentifier + "!";
    }
}
