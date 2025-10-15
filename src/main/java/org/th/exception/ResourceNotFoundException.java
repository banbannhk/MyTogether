package org.th.exception;

// ============ DATABASE EXCEPTIONS ============
public class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
