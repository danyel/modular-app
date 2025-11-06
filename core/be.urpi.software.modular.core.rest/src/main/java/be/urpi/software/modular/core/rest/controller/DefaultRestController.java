package be.urpi.software.modular.core.rest.controller;

import be.urpi.software.modular.core.rest.api.service.RestService;
import be.urpi.software.modular.core.rest.api.service.exception.OnGetException;
import be.urpi.software.modular.core.rest.api.service.exception.RestServiceNameNotFoundException;
import be.urpi.software.modular.core.rest.api.service.manager.RestServiceManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RequestMapping(value = "/{" + DefaultRestController.REST_SERVICE_NAME + "}")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DefaultRestController {
    static final String REST_SERVICE_NAME = "restServiceName";
    static final String UNIQUE_IDENTIFIER = "uniqueIdentifier";
    private final RestServiceManager restServiceManager;

    @RequestMapping(value = "/{" + UNIQUE_IDENTIFIER + "}")
    ResponseEntity<Object> get(@PathVariable(value = REST_SERVICE_NAME) String restServiceName, @PathVariable(value = UNIQUE_IDENTIFIER) Object uniqueIdentifier) throws RestServiceNameNotFoundException {
        RestService<Object, Object> restService = restServiceManager.locateByName(restServiceName);
        try {
            Object result = restService.doOnGet(uniqueIdentifier);
            return new ResponseEntity<>(result, OK);
        } catch (final OnGetException onGetException) {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }
}
