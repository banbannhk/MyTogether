package org.th.exception;

public class InvalidParameterException extends ValidationException {
    public InvalidParameterException(String parameter, String reason) {
        super("Invalid parameter '" + parameter + "': " + reason);
    }
}
