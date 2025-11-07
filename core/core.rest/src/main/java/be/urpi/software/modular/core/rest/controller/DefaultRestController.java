package be.urpi.software.modular.core.rest.controller;

import be.urpi.software.modular.core.service.RestService;
import be.urpi.software.modular.core.service.exception.OnGetException;
import be.urpi.software.modular.core.service.exception.RestServiceNameNotFoundException;
import be.urpi.software.modular.core.service.manager.RestServiceManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RequestMapping(value = "/configuration/{" + DefaultRestController.REST_SERVICE_NAME + "}")
@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DefaultRestController implements ApplicationContextAware {
    static final String REST_SERVICE_NAME = "restServiceName";
    static final String UNIQUE_IDENTIFIER = "uniqueIdentifier";
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/{" + UNIQUE_IDENTIFIER + "}")
    ResponseEntity<Object> get(@PathVariable(value = REST_SERVICE_NAME) String restServiceName, @PathVariable(value = UNIQUE_IDENTIFIER) Object uniqueIdentifier) throws RestServiceNameNotFoundException {
        RestServiceManager restServiceManager = applicationContext.getBean(RestServiceManager.class);
        RestService<Object, Object> restService = restServiceManager.locateByName(restServiceName);
        try {
            Object result = restService.doOnGet(uniqueIdentifier);
            return new ResponseEntity<>(result, OK);
        } catch (OnGetException onGetException) {
            return new ResponseEntity<>(BAD_REQUEST);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
