package be.urpi.software.modular.core.service.exception;

public class RestServiceNameNotFoundException extends Exception {

    RestServiceNameNotFoundException(final String restServiceName) {
        super(String.format("%s not found", restServiceName));
    }

    public static RestServiceNameNotFoundException forServiceName(final String restServiceName) {
        return new RestServiceNameNotFoundException(restServiceName);
    }
}
