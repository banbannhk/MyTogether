package org.th.exception;

public class InvalidCoordinateException extends ValidationException {
    public InvalidCoordinateException(String coordinate) {
        super("Invalid coordinate: " + coordinate);
    }
}
