package be.urpi.software.modular.core.rest.api.service.manager;

import be.urpi.software.modular.core.rest.api.service.RestService;
import be.urpi.software.modular.core.rest.api.service.exception.RestServiceNameNotFoundException;

public interface RestServiceManager {
    <S, E> RestService<S, E> locateByName(String restServiceName) throws RestServiceNameNotFoundException;
}
