package org.th.exception;

public class DuplicateResourceException extends DatabaseException {
    public DuplicateResourceException(String message) {
        super(message, null);
    }
}
