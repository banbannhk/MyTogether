package org.th.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
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
@lombok.extern.slf4j.Slf4j
public class GlobalExceptionHandler {

        // ============ DATABASE EXCEPTIONS ============

        @ExceptionHandler(DeviceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleDeviceNotFound(
                        DeviceNotFoundException ex, HttpServletRequest request) {
                log.error("Device not found: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "DEVICE_NOT_FOUND",
                                "Device Not Found",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.NOT_FOUND.value());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUserNotFound(
                        UserNotFoundException ex, HttpServletRequest request) {
                log.error("User not found: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.USER_NOT_FOUND.getCode(),
                                "User Not Found",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.NOT_FOUND.value());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(RouteNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleRouteNotFound(
                        RouteNotFoundException ex, HttpServletRequest request) {
                log.error("Route not found: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Route Not Found",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.NOT_FOUND.value());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(
                        ResourceNotFoundException ex, HttpServletRequest request) {
                log.error("Resource not found: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                                "Resource Not Found",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.NOT_FOUND.value());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateResource(
                        DuplicateResourceException ex, HttpServletRequest request) {
                log.error("Duplicate resource: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.DUPLICATE_RESOURCE.getCode(),
                                "Resource Already Exists",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.CONFLICT.value());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(DatabaseException.class)
        public ResponseEntity<ErrorResponse> handleDatabaseError(
                        DatabaseException ex, HttpServletRequest request) {
                log.error("Database error: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Database Operation Failed",
                                "An error occurred while processing your request. Please try again later.",
                                request.getRequestURI(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException ex, HttpServletRequest request) {
                log.error("Data integrity violation: {}", ex.getMessage(), ex);

                String message = "Data conflict occurred";
                String code = ErrorCode.DUPLICATE_RESOURCE.getCode();

                if (ex.getMessage() != null) {
                        if (ex.getMessage().contains("duplicate") || ex.getMessage().contains("Duplicate")) {
                                message = "Resource already exists";
                        } else if (ex.getMessage().contains("foreign key") || ex.getMessage().contains("constraint")) {
                                message = "Cannot delete resource - it is being used";
                                code = "CONSTRAINT_VIOLATION";
                        }
                }

                ErrorResponse error = new ErrorResponse(
                                code,
                                "Data Integrity Error",
                                message,
                                request.getRequestURI(),
                                HttpStatus.CONFLICT.value());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(EmptyResultDataAccessException.class)
        public ResponseEntity<ErrorResponse> handleEmptyResult(
                        EmptyResultDataAccessException ex, HttpServletRequest request) {
                log.error("Empty result from database: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Resource Not Found",
                                "The requested resource does not exist",
                                request.getRequestURI(),
                                HttpStatus.NOT_FOUND.value());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        // ============ VALIDATION EXCEPTIONS ============

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ErrorResponse> handleValidation(
                        ValidationException ex, HttpServletRequest request) {
                log.error("Validation error: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.VALIDATION_FAILED.getCode(),
                                "Validation Failed",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST.value());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(InvalidCoordinateException.class)
        public ResponseEntity<ErrorResponse> handleInvalidCoordinate(
                        InvalidCoordinateException ex, HttpServletRequest request) {
                log.error("Invalid coordinate: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Invalid Coordinate",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST.value());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(InvalidParameterException.class)
        public ResponseEntity<ErrorResponse> handleInvalidParameter(
                        InvalidParameterException ex, HttpServletRequest request) {
                log.error("Invalid parameter: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Invalid Parameter",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST.value());

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

                log.error("Request validation failed: {}", errors);

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.VALIDATION_FAILED.getCode(),
                                "Validation Failed",
                                "Request validation failed. Please check your input.",
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST.value());
                error.setValidationErrors(errors);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingParameter(
                        MissingServletRequestParameterException ex, HttpServletRequest request) {
                log.error("Missing request parameter: {}", ex.getParameterName());

                ErrorResponse error = new ErrorResponse(
                                "Missing Parameter",
                                "Required parameter '" + ex.getParameterName() + "' is missing",
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST.value());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
                log.error("Type mismatch for parameter: {}", ex.getName());

                Class<?> requiredType = ex.getRequiredType();
                String typeName = requiredType != null ? requiredType.getSimpleName() : "unknown";
                String detail = String.format("Parameter '%s' should be of type '%s'", ex.getName(), typeName);

                ErrorResponse error = new ErrorResponse(
                                "Invalid Parameter Type",
                                detail,
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST.value());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // ============ HTTP/REST EXCEPTIONS ============

        @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoResourceFound(
                        org.springframework.web.servlet.resource.NoResourceFoundException ex,
                        HttpServletRequest request) {
                log.error("Resource not found: {}", ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                                "Resource Not Found",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.NOT_FOUND.value());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleMessageNotReadable(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {
                log.error("Malformed JSON request: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Malformed Request",
                                "Request body is not readable. Please check your JSON format.",
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST.value());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleMaxSizeException(
                        MaxUploadSizeExceededException ex, HttpServletRequest request) {
                log.error("File size exceeded: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "File Too Large",
                                "Maximum upload size exceeded. Please upload a smaller file.",
                                request.getRequestURI(),
                                HttpStatus.PAYLOAD_TOO_LARGE.value());

                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
        }

        // ============ REST CLIENT EXCEPTIONS (NEW!) ============

        /**
         * Handle HTTP 4xx errors from RestTemplate (Bad Request, Unauthorized,
         * Forbidden, Not Found, etc.)
         */
        @ExceptionHandler(HttpClientErrorException.class)
        public ResponseEntity<ErrorResponse> handleHttpClientError(
                        HttpClientErrorException ex, HttpServletRequest request) {
                log.error("HTTP Client Error [{}]: {}", ex.getStatusCode(), ex.getMessage(), ex);

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
                                ex.getStatusCode().value());

                return ResponseEntity.status(ex.getStatusCode()).body(error);
        }

        /**
         * Handle HTTP 5xx errors from RestTemplate (Internal Server Error, Bad Gateway,
         * Service Unavailable)
         */
        @ExceptionHandler(HttpServerErrorException.class)
        public ResponseEntity<ErrorResponse> handleHttpServerError(
                        HttpServerErrorException ex, HttpServletRequest request) {
                log.error("HTTP Server Error [{}]: {}", ex.getStatusCode(), ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "External Service Error",
                                "The external service is experiencing issues. Please try again later.",
                                request.getRequestURI(),
                                HttpStatus.BAD_GATEWAY.value());

                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
        }

        /**
         * Handle generic RestTemplate exceptions (connection timeout, network issues,
         * etc.)
         * This should be AFTER HttpClientErrorException and HttpServerErrorException
         */
        @ExceptionHandler(RestClientException.class)
        public ResponseEntity<ErrorResponse> handleRestClientException(
                        RestClientException ex, HttpServletRequest request) {
                log.error("REST Client Exception: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Service Unavailable",
                                "Unable to reach external service. Please try again later.",
                                request.getRequestURI(),
                                HttpStatus.SERVICE_UNAVAILABLE.value());

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }

        // ============ EXTERNAL API EXCEPTIONS ============

        @ExceptionHandler(GoogleMapsQuotaExceededException.class)
        public ResponseEntity<ErrorResponse> handleQuotaExceeded(
                        GoogleMapsQuotaExceededException ex, HttpServletRequest request) {
                log.error("Google Maps quota exceeded: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Service Temporarily Unavailable",
                                "Map service quota exceeded. Please try again later.",
                                request.getRequestURI(),
                                HttpStatus.TOO_MANY_REQUESTS.value());

                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
        }

        @ExceptionHandler(GoogleMapsAuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleGoogleMapsAuth(
                        GoogleMapsAuthenticationException ex, HttpServletRequest request) {
                log.error("Google Maps authentication error: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Service Configuration Error",
                                "Map service is temporarily unavailable. Please contact support.",
                                request.getRequestURI(),
                                HttpStatus.SERVICE_UNAVAILABLE.value());

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }

        @ExceptionHandler(GoogleMapsInvalidRequestException.class)
        public ResponseEntity<ErrorResponse> handleGoogleMapsInvalidRequest(
                        GoogleMapsInvalidRequestException ex, HttpServletRequest request) {
                log.error("Google Maps invalid request: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Invalid Route Request",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.BAD_REQUEST.value());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(GoogleMapsException.class)
        public ResponseEntity<ErrorResponse> handleGoogleMapsError(
                        GoogleMapsException ex, HttpServletRequest request) {
                log.error("Google Maps error: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Map Service Error",
                                "Unable to fetch route information. Please try again.",
                                request.getRequestURI(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        @ExceptionHandler(ExternalApiException.class)
        public ResponseEntity<ErrorResponse> handleExternalApiError(
                        ExternalApiException ex, HttpServletRequest request) {
                log.error("External API error [{}]: {}", ex.getApiName(), ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "External Service Error",
                                "Failed to communicate with external service: " + ex.getApiName(),
                                request.getRequestURI(),
                                HttpStatus.BAD_GATEWAY.value());

                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
        }

        // ============ BUSINESS LOGIC EXCEPTIONS ============

        @ExceptionHandler(InactiveDeviceException.class)
        public ResponseEntity<ErrorResponse> handleInactiveDevice(
                        InactiveDeviceException ex, HttpServletRequest request) {
                log.error("Inactive device: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Device Inactive",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.FORBIDDEN.value());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        @ExceptionHandler(UnauthorizedAccessException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
                        UnauthorizedAccessException ex, HttpServletRequest request) {
                log.error("Unauthorized access: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.UNAUTHORIZED.getCode(),
                                "Unauthorized Access",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.FORBIDDEN.value());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                        AccessDeniedException ex, HttpServletRequest request) {
                log.error("Access denied: {}", ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.FORBIDDEN.getCode(),
                                "Access Denied",
                                "You do not have permission to access this resource",
                                request.getRequestURI(),
                                HttpStatus.FORBIDDEN.value());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                        BadCredentialsException ex, HttpServletRequest request) {
                log.error("Bad credentials: {}", ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.INVALID_CREDENTIALS.getCode(),
                                "Authentication Failed",
                                "Invalid username or password",
                                request.getRequestURI(),
                                HttpStatus.UNAUTHORIZED.value());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusinessException(
                        BusinessException ex, HttpServletRequest request) {
                log.error("Business logic error: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                ErrorCode.BUSINESS_RULE_VIOLATION.getCode(),
                                "Business Rule Violation",
                                ex.getMessage(),
                                request.getRequestURI(),
                                HttpStatus.UNPROCESSABLE_ENTITY.value());

                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }

        // ============ FILE EXCEPTIONS ============

        @ExceptionHandler(FileProcessingException.class)
        public ResponseEntity<ErrorResponse> handleFileProcessing(
                        FileProcessingException ex, HttpServletRequest request) {
                log.error("File processing error: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "File Processing Error",
                                "Failed to process the uploaded file. Please check the file format.",
                                request.getRequestURI(),
                                HttpStatus.UNPROCESSABLE_ENTITY.value());

                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
        }

        // ============ GENERIC APPLICATION EXCEPTION ============

        @ExceptionHandler(ApplicationException.class)
        public ResponseEntity<ErrorResponse> handleApplicationException(
                        ApplicationException ex, HttpServletRequest request) {
                log.error("Application error: {}", ex.getMessage(), ex);

                ErrorCode code = ex.getErrorCode();
                if (code == null) {
                        code = ErrorCode.INTERNAL_SERVER_ERROR;
                }

                ErrorResponse error = new ErrorResponse(
                                code.getCode(),
                                "Application Error",
                                ex.getMessage(),
                                request.getRequestURI(),
                                code.getStatus().value());

                return ResponseEntity.status(code.getStatus().value()).body(error);
        }

        // ============ FALLBACK EXCEPTION (MUST BE LAST!) ============

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {
                log.error("Unexpected error: {}", ex.getMessage(), ex);

                ErrorResponse error = new ErrorResponse(
                                "Internal Server Error",
                                "An unexpected error occurred. Please contact support if the problem persists.",
                                request.getRequestURI(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}