package org.th.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.th.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ============ DATABASE EXCEPTIONS ============

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDeviceNotFound(
            DeviceNotFoundException ex, HttpServletRequest request) {
        logger.error("Device not found: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "Device Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {
        logger.error("User not found: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "User Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRouteNotFound(
            RouteNotFoundException ex, HttpServletRequest request) {
        logger.error("Route not found: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Route Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        logger.error("Resource not found: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        logger.error("Duplicate resource: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Resource Already Exists",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.CONFLICT.value()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseError(
            DatabaseException ex, HttpServletRequest request) {
        logger.error("Database error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "Database Operation Failed",
                "An error occurred while processing your request. Please try again later.",
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage(), ex );

        String message = "Data conflict occurred";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("duplicate") || ex.getMessage().contains("Duplicate")) {
                message = "Resource already exists";
            } else if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("constraint")) {
                message = "Cannot delete resource - it is being used";
            }
        }

        ErrorResponse error = new ErrorResponse(
                "Data Integrity Error",
                message,
                request.getRequestURI(),
                HttpStatus.CONFLICT.value()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleEmptyResult(
            EmptyResultDataAccessException ex, HttpServletRequest request) {
        logger.error("Empty result from database: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Resource Not Found",
                "The requested resource does not exist",
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ============ VALIDATION EXCEPTIONS ============

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, HttpServletRequest request) {
        logger.error("Validation error: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Validation Failed",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidCoordinateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCoordinate(
            InvalidCoordinateException ex, HttpServletRequest request) {
        logger.error("Invalid coordinate: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Invalid Coordinate",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(
            InvalidParameterException ex, HttpServletRequest request) {
        logger.error("Invalid parameter: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Invalid Parameter",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.error("Request validation failed: {}", errors);

        ErrorResponse error = new ErrorResponse(
                "Validation Failed",
                "Request validation failed. Please check your input.",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );
        error.setValidationErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.error("Missing request parameter: {}", ex.getParameterName());

        ErrorResponse error = new ErrorResponse(
                "Missing Parameter",
                "Required parameter '" + ex.getParameterName() + "' is missing",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.error("Type mismatch for parameter: {}", ex.getName());

        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(), ex.getRequiredType().getSimpleName());

        ErrorResponse error = new ErrorResponse(
                "Invalid Parameter Type",
                message,
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ============ HTTP/REST EXCEPTIONS ============

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.error("Malformed JSON request: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Malformed Request",
                "Request body is not readable. Please check your JSON format.",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        logger.error("File size exceeded: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "File Too Large",
                "Maximum upload size exceeded. Please upload a smaller file.",
                request.getRequestURI(),
                HttpStatus.PAYLOAD_TOO_LARGE.value()
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    // ============ REST CLIENT EXCEPTIONS (NEW!) ============

    /**
     * Handle HTTP 4xx errors from RestTemplate (Bad Request, Unauthorized, Forbidden, Not Found, etc.)
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(
            HttpClientErrorException ex, HttpServletRequest request) {
        logger.error("HTTP Client Error [{}]: {}", ex.getStatusCode(), ex.getMessage(), ex );

        String details = "External API request failed";

        // Provide more specific messages based on status code
        if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
            details = "Invalid request sent to external service";
        } else if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            details = "Authentication failed with external service";
        } else if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            details = "Access denied by external service";
        } else if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            details = "Resource not found in external service";
        }

        ErrorResponse error = new ErrorResponse(
                "API Request Failed",
                details,
                request.getRequestURI(),
                ex.getStatusCode().value()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    /**
     * Handle HTTP 5xx errors from RestTemplate (Internal Server Error, Bad Gateway, Service Unavailable)
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerError(
            HttpServerErrorException ex, HttpServletRequest request) {
        logger.error("HTTP Server Error [{}]: {}", ex.getStatusCode(), ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "External Service Error",
                "The external service is experiencing issues. Please try again later.",
                request.getRequestURI(),
                HttpStatus.BAD_GATEWAY.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    /**
     * Handle generic RestTemplate exceptions (connection timeout, network issues, etc.)
     * This should be AFTER HttpClientErrorException and HttpServerErrorException
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(
            RestClientException ex, HttpServletRequest request) {
        logger.error("REST Client Exception: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "Service Unavailable",
                "Unable to reach external service. Please try again later.",
                request.getRequestURI(),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    // ============ EXTERNAL API EXCEPTIONS ============

    @ExceptionHandler(GoogleMapsQuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handleQuotaExceeded(
            GoogleMapsQuotaExceededException ex, HttpServletRequest request) {
        logger.error("Google Maps quota exceeded: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Service Temporarily Unavailable",
                "Map service quota exceeded. Please try again later.",
                request.getRequestURI(),
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(GoogleMapsAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleGoogleMapsAuth(
            GoogleMapsAuthenticationException ex, HttpServletRequest request) {
        logger.error("Google Maps authentication error: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Service Configuration Error",
                "Map service is temporarily unavailable. Please contact support.",
                request.getRequestURI(),
                HttpStatus.SERVICE_UNAVAILABLE.value()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(GoogleMapsInvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleGoogleMapsInvalidRequest(
            GoogleMapsInvalidRequestException ex, HttpServletRequest request) {
        logger.error("Google Maps invalid request: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Invalid Route Request",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(GoogleMapsException.class)
    public ResponseEntity<ErrorResponse> handleGoogleMapsError(
            GoogleMapsException ex, HttpServletRequest request) {
        logger.error("Google Maps error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "Map Service Error",
                "Unable to fetch route information. Please try again.",
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiError(
            ExternalApiException ex, HttpServletRequest request) {
        logger.error("External API error [{}]: {}", ex.getApiName(), ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "External Service Error",
                "Failed to communicate with external service: " + ex.getApiName(),
                request.getRequestURI(),
                HttpStatus.BAD_GATEWAY.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    // ============ BUSINESS LOGIC EXCEPTIONS ============

    @ExceptionHandler(InactiveDeviceException.class)
    public ResponseEntity<ErrorResponse> handleInactiveDevice(
            InactiveDeviceException ex, HttpServletRequest request) {
        logger.error("Inactive device: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Device Inactive",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, HttpServletRequest request) {
        logger.error("Unauthorized access: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Unauthorized Access",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        logger.error("Business logic error: {}", ex.getMessage(), ex );

        ErrorResponse error = new ErrorResponse(
                "Business Rule Violation",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.UNPROCESSABLE_ENTITY.value()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // ============ FILE EXCEPTIONS ============

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponse> handleFileProcessing(
            FileProcessingException ex, HttpServletRequest request) {
        logger.error("File processing error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "File Processing Error",
                "Failed to process the uploaded file. Please check the file format.",
                request.getRequestURI(),
                HttpStatus.UNPROCESSABLE_ENTITY.value()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // ============ GENERIC APPLICATION EXCEPTION ============

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(
            ApplicationException ex, HttpServletRequest request) {
        logger.error("Application error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "Application Error",
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ============ FALLBACK EXCEPTION (MUST BE LAST!) ============

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An unexpected error occurred. Please contact support if the problem persists.",
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}