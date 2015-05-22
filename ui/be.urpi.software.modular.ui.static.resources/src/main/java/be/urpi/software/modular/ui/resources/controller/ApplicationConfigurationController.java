package be.urpi.software.modular.ui.resources.controller;

import be.urpi.software.modular.core.properties.WatchAbleApplicationProperties;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class ApplicationConfigurationController {
    @Resource
    private WatchAbleApplicationProperties watchAbleApplicationProperties;

    @ResponseStatus(OK)
    @RequestMapping(value = "/applicationConfiguration", method = GET)
//, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    WatchAbleApplicationProperties get() {
        return watchAbleApplicationProperties;
    }
}
