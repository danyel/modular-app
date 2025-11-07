package com.github.test.service;

import be.urpi.software.modular.core.rest.api.service.RestService;

public class HelloService implements RestService<String, String> {
    public String doOnGet(String uniqueIdentifier) {
        return uniqueIdentifier + "!";
    }
}
