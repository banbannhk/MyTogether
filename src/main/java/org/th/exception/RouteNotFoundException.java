package org.th.exception;

public class RouteNotFoundException extends ResourceNotFoundException {
    public RouteNotFoundException(Long routeId) {
        super("Route not found: " + routeId);
    }
}
