package be.urpi.software.modular.core.rest.api.service;

import be.urpi.software.modular.core.rest.api.service.exception.OnGetException;

public interface RestService<S, W> {
    S doOnGet(W uniqueIdentifier) throws OnGetException;
}
