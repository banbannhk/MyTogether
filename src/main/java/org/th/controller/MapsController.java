package org.th.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.th.dto.ApiResponse;
import org.th.service.DeviceTrackingService;
import org.th.service.GoogleMapsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maps")
@CrossOrigin(origins = "*")
public class MapsController {

    @Autowired
    private GoogleMapsService mapsService;

    @Autowired
    private DeviceTrackingService deviceTrackingService;

    /**
     * Search Myanmar shops from Google Places API
     * GET /api/maps/myanmar-shops?query=Myanmar restaurant Bangkok
     */
    @GetMapping("/myanmar-shops")
    public ResponseEntity<ApiResponse<JsonNode>> searchMyanmarShops(
            @RequestParam(required = false, defaultValue = "Myanmar restaurant Bangkok") String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "5000") Integer radius) {

        JsonNode result = mapsService.searchMyanmarShops(query, lat, lng, radius);
        return ResponseEntity.ok(
                ApiResponse.success("Myanmar shops retrieved successfully", result)
        );
    }

    /**
     * Health check endpoint
     * GET /api/maps/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Google Maps API is working!", "Service is healthy")
        );
    }

    /**
     * Get transit route (bus/train)
     * GET /api/maps/transit-route?origin=Bangkok&destination=Pattaya
     */
    @GetMapping("/transit-route")
    public ResponseEntity<ApiResponse<JsonNode>> getTransitRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String deviceId,
            HttpServletRequest request) {

        String ipAddress = deviceTrackingService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        JsonNode result = mapsService.getTransitRoute(origin, destination, deviceId, ipAddress, userAgent);
        return ResponseEntity.ok(
                ApiResponse.success("Transit route retrieved successfully", result)
        );
    }

    /**
     * Get BUS-only route
     * GET /api/maps/bus-route?origin=Bangkok&destination=Chatuchak
     */
    @GetMapping("/bus-route")
    public ResponseEntity<ApiResponse<JsonNode>> getBusRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String deviceId,
            HttpServletRequest request) {

        String ipAddress = deviceTrackingService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        JsonNode result = mapsService.getBusRoute(origin, destination, deviceId, ipAddress, userAgent);
        return ResponseEntity.ok(
                ApiResponse.success("Bus route retrieved successfully", result)
        );
    }

    /**
     * Get TRAIN/SUBWAY-only route (BTS/MRT)
     * GET /api/maps/train-route?origin=Siam&destination=Asok
     */
    @GetMapping("/train-route")
    public ResponseEntity<ApiResponse<JsonNode>> getTrainRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String deviceId,
            HttpServletRequest request) {

        String ipAddress = deviceTrackingService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        JsonNode result = mapsService.getTrainRoute(origin, destination, deviceId, ipAddress, userAgent);
        return ResponseEntity.ok(
                ApiResponse.success("Train/Subway route retrieved successfully", result)
        );
    }

    /**
     * Get detailed transit routes with mode information
     * GET /api/maps/detailed-transit-route?origin=Bangkok&destination=Pattaya
     */
    @GetMapping("/detailed-transit-route")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetailedTransitRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String deviceId,
            HttpServletRequest request) {

        Map<String, Object> result = mapsService.getDetailedTransitRoute(origin, destination);
        return ResponseEntity.ok(
                ApiResponse.success("Detailed transit routes retrieved", result)
        );
    }

    /**
     * Get driving route
     * GET /api/maps/driving-route?origin=Bangkok&destination=Chiang Mai
     */
    @GetMapping("/driving-route")
    public ResponseEntity<ApiResponse<JsonNode>> getDrivingRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String deviceId,
            HttpServletRequest request) {

        String ipAddress = deviceTrackingService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        JsonNode result = mapsService.getDrivingRoute(origin, destination, deviceId, ipAddress, userAgent);
        return ResponseEntity.ok(
                ApiResponse.success("Driving route retrieved successfully", result)
        );
    }

    /**
     * Get walking route
     * GET /api/maps/walking-route?origin=Siam&destination=Asok
     */
    @GetMapping("/walking-route")
    public ResponseEntity<ApiResponse<JsonNode>> getWalkingRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String deviceId,
            HttpServletRequest request) {

        String ipAddress = deviceTrackingService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        JsonNode result = mapsService.getWalkingRoute(origin, destination, deviceId, ipAddress, userAgent);
        return ResponseEntity.ok(
                ApiResponse.success("Walking route retrieved successfully", result)
        );
    }

    /**
     * Get bicycling route
     * GET /api/maps/bicycling-route?origin=Lumpini&destination=Chatuchak
     */
    @GetMapping("/bicycling-route")
    public ResponseEntity<ApiResponse<JsonNode>> getBicyclingRoute(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String deviceId,
            HttpServletRequest request) {

        String ipAddress = deviceTrackingService.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        JsonNode result = mapsService.getBicyclingRoute(origin, destination, deviceId, ipAddress, userAgent);
        return ResponseEntity.ok(
                ApiResponse.success("Bicycling route retrieved successfully", result)
        );
    }

    /**
     * Search places
     * GET /api/maps/search?query=restaurants near Bangkok
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<JsonNode>> searchPlaces(@RequestParam String query) {
        JsonNode result = mapsService.searchPlaces(query);
        return ResponseEntity.ok(
                ApiResponse.success("Places search completed", result)
        );
    }

    /**
     * Get route with multiple waypoints
     * POST /api/maps/route-with-waypoints
     * Body: {"origin": "Bangkok", "destination": "Chiang Mai", "waypoints": ["Ayutthaya", "Lopburi"]}
     */
    @PostMapping("/route-with-waypoints")
    public ResponseEntity<ApiResponse<JsonNode>> getRouteWithWaypoints(
            @RequestBody Map<String, Object> request) {

        String origin = (String) request.get("origin");
        String destination = (String) request.get("destination");

        @SuppressWarnings("unchecked")
        List<String> waypointsList = (List<String>) request.get("waypoints");
        String[] waypoints = waypointsList != null ? waypointsList.toArray(new String[0]) : new String[0];

        JsonNode result = mapsService.getRouteWithWaypoints(origin, destination, waypoints);
        return ResponseEntity.ok(
                ApiResponse.success("Route with waypoints retrieved successfully", result)
        );
    }
}