package be.urpi.software.modular.core.service;

import be.urpi.software.modular.core.service.exception.OnGetException;

public interface RestService<S, W> {
    S doOnGet(W uniqueIdentifier) throws OnGetException;
}
