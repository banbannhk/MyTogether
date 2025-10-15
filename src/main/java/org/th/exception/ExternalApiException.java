package org.th.exception;

public class ExternalApiException extends ApplicationException {
    private final String apiName;

    public ExternalApiException(String apiName, String message, Throwable cause) {
        super(message, cause);
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }
}
