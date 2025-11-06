package com.github.test.service;

import be.urpi.software.modular.core.rest.api.service.RestService;
import be.urpi.software.modular.core.rest.api.service.exception.OnGetException;

public class HelloService implements RestService<String, String> {
    public String doOnGet(String uniqueIdentifier) throws OnGetException {
        return uniqueIdentifier + "!";
    }
}
