package org.th.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // General
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    INVALID_REQUEST("INVALID_REQUEST", HttpStatus.BAD_REQUEST, "Invalid request parameters"),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Validation failed"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),

    // Auth & User
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Unauthorized access"),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "Access forbidden"),
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", HttpStatus.CONFLICT, "Username already exists"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "Email already exists"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid username or password"),

    // Cart
    CART_MULTI_SHOP_CONFLICT("CART_MULTI_SHOP_CONFLICT", HttpStatus.CONFLICT,
            "Cart cannot contain items from multiple shops"),
    CART_ITEM_NOT_FOUND("CART_ITEM_NOT_FOUND", HttpStatus.NOT_FOUND, "Cart item not found"),

    // Business Logic
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", HttpStatus.CONFLICT, "Resource already exists"),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violation");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
}
