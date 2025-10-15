package org.th.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.th.exception.GoogleMapsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleMapsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsService.class);

    @Value("${google.maps.api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DeviceTrackingService deviceTrackingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";
    private static final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";

    /**
     * Get transit route (bus/train)
     */
    public JsonNode getTransitRoute(String origin, String destination, String deviceId, String ipAddress, String userAgent) {
        logger.info("Fetching transit route from {} to {}", origin, destination);

        JsonNode routeData = getDirections(origin, destination, "transit", true);

        String routeType = "TRANSIT";
        // Fire-and-forget async tracking (NON-BLOCKING)
        deviceTrackingService.trackDeviceAndRouteAsync(
                deviceId, origin, destination, routeType, ipAddress, userAgent, routeData);

        return routeData;

    }

    /**
     * Get driving route
     */
    public JsonNode getDrivingRoute(String origin, String destination, String deviceId, String ipAddress, String userAgent) {
        logger.info("Fetching driving route from {} to {}", origin, destination);
        JsonNode routeData = getDirections(origin, destination, "driving", true);

        String routeType = "DRIVING";
        // Fire-and-forget async tracking (NON-BLOCKING)
        deviceTrackingService.trackDeviceAndRouteAsync(
                deviceId, origin, destination, routeType, ipAddress, userAgent, routeData);

        return routeData;
    }

    /**
     * Get walking route
     */
    public JsonNode getWalkingRoute(String origin, String destination, String deviceId, String ipAddress, String userAgent) {
        logger.info("Fetching walking route from {} to {}", origin, destination);
        JsonNode routeData = getDirections(origin, destination, "walking", false);

        String routeType = "WALKING";
        // Fire-and-forget async tracking (NON-BLOCKING)
        deviceTrackingService.trackDeviceAndRouteAsync(
                deviceId, origin, destination, routeType, ipAddress, userAgent, routeData);

        return routeData;
    }

    /**
     * Get bicycling route
     */
    public JsonNode getBicyclingRoute(String origin, String destination, String deviceId, String ipAddress, String userAgent) {
        logger.info("Fetching bicycling route from {} to {}", origin, destination);
        JsonNode routeData = getDirections(origin, destination, "bicycling", false);
        String routeType = "BICYCLING";
        // Fire-and-forget async tracking (NON-BLOCKING)
        deviceTrackingService.trackDeviceAndRouteAsync(
                deviceId, origin, destination, routeType, ipAddress, userAgent, routeData);

        return routeData;
    }

    /**
     * Get route with waypoints
     */
    public JsonNode getRouteWithWaypoints(String origin, String destination, String[] waypoints) {
        logger.info("Fetching route with waypoints from {} to {}", origin, destination);
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(DIRECTIONS_API_URL)
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .queryParam("mode", "driving")
                    .queryParam("optimize", "true")
                    .queryParam("key", apiKey);

            // Add waypoints
            if (waypoints != null && waypoints.length > 0) {
                String waypointsStr = "optimize:true|" + String.join("|", waypoints);
                builder.queryParam("waypoints", waypointsStr);
            }

            String url = builder.toUriString();
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonResponse = objectMapper.readTree(response);

            validateGoogleMapsResponse(jsonResponse);
            return jsonResponse;

        } catch (RestClientException e) {
            logger.error("Failed to connect to Google Maps API", e);
            throw new GoogleMapsException("Failed to connect to Google Maps API", e);
        } catch (Exception e) {
            logger.error("Error processing Google Maps response", e);
            throw new GoogleMapsException("Error processing Google Maps response", e);
        }
    }

    /**
     * Search places
     */
    public JsonNode searchPlaces(String query) {
        logger.info("Searching places for query: {}", query);
        try {
            String url = UriComponentsBuilder.fromHttpUrl(PLACES_API_URL)
                    .queryParam("query", query)
                    .queryParam("key", apiKey)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonResponse = objectMapper.readTree(response);

            validateGoogleMapsResponse(jsonResponse);
            return jsonResponse;

        } catch (RestClientException e) {
            logger.error("Failed to connect to Google Maps API", e);
            throw new GoogleMapsException("Failed to connect to Google Maps API", e);
        } catch (Exception e) {
            logger.error("Error processing Google Maps response", e);
            throw new GoogleMapsException("Error processing Google Maps response", e);
        }
    }

    /**
     * Validate Google Maps API response
     */
    private void validateGoogleMapsResponse(JsonNode jsonResponse) {
        String status = jsonResponse.get("status").asText();

        switch (status) {
            case "OK":
            case "ZERO_RESULTS":
                // Valid responses
                break;
            case "REQUEST_DENIED":
                String errorMessage = jsonResponse.has("error_message")
                        ? jsonResponse.get("error_message").asText()
                        : "API key invalid or APIs not enabled";
                throw new GoogleMapsException("Request denied: " + errorMessage);
            case "OVER_QUERY_LIMIT":
                throw new GoogleMapsException("API quota exceeded");
            case "INVALID_REQUEST":
                throw new GoogleMapsException("Invalid request parameters");
            default:
                throw new GoogleMapsException("API returned status: " + status);
        }
    }

    /**
     * Get BUS-only route
     */
    public JsonNode getBusRoute(String origin, String destination, String deviceId, String ipAddress, String userAgent) {
        logger.info("Fetching BUS route from {} to {}", origin, destination);

        Map<String, String> busParams = new HashMap<>();
        busParams.put("transit_mode", "bus");
        busParams.put("transit_routing_preference", "fewer_transfers");
        busParams.put("departure_time", "now");

        JsonNode routeData = getDirections(origin, destination, "transit", true, busParams);

        String routeType = "BUS";
        // Fire-and-forget async tracking (NON-BLOCKING)
        deviceTrackingService.trackDeviceAndRouteAsync(
                deviceId, origin, destination, routeType, ipAddress, userAgent, routeData);

        return routeData;
    }

    /**
     * Get TRAIN/SUBWAY-only route (BTS/MRT)
     */
    public JsonNode getTrainRoute(String origin, String destination, String deviceId, String ipAddress, String userAgent) {
        Map<String, String> trainParams = new HashMap<>();
        trainParams.put("transit_mode", "subway|train|rail");
        trainParams.put("transit_routing_preference", "less_walking");
        trainParams.put("departure_time", "now");

        JsonNode routeData = getDirections(origin, destination, "transit", true, trainParams);

        String routeType = "TRAIN";
        // Fire-and-forget async tracking (NON-BLOCKING)
        deviceTrackingService.trackDeviceAndRouteAsync(
                deviceId, origin, destination, routeType, ipAddress, userAgent, routeData);

        return routeData;
    }

    /**
     * Get detailed transit route with mode information
     */
    public Map<String, Object> getDetailedTransitRoute(String origin, String destination) {
        logger.info("Fetching detailed transit route from {} to {}", origin, destination);

        Map<String, String> transitParams = new HashMap<>();
        transitParams.put("transit_mode", "bus|subway|train");
        transitParams.put("departure_time", "now");

        JsonNode routeData = getDirections(origin, destination, "transit", true, transitParams);

        // Extract transit modes used
        List<Map<String, Object>> routesWithModes = new ArrayList<>();

        if (routeData.has("routes")) {
            JsonNode routes = routeData.get("routes");

            for (JsonNode route : routes) {
                Map<String, Object> routeInfo = new HashMap<>();
                List<String> transitModes = new ArrayList<>();
                boolean hasBus = false;
                boolean hasTrain = false;
                boolean hasSubway = false;

                if (route.has("legs")) {
                    JsonNode legs = route.get("legs");
                    for (JsonNode leg : legs) {
                        if (leg.has("steps")) {
                            JsonNode steps = leg.get("steps");
                            for (JsonNode step : steps) {
                                if (step.has("transit_details")) {
                                    JsonNode transit = step.get("transit_details");
                                    String vehicleType = transit.get("line").get("vehicle").get("type").asText();
                                    String lineName = transit.get("line").get("name").asText();

                                    if ("BUS".equals(vehicleType)) {
                                        hasBus = true;
                                        transitModes.add("Bus: " + lineName);
                                    } else if ("SUBWAY".equals(vehicleType)) {
                                        hasSubway = true;
                                        transitModes.add("BTS/MRT: " + lineName);
                                    } else if ("HEAVY_RAIL".equals(vehicleType) || "COMMUTER_TRAIN".equals(vehicleType)) {
                                        hasTrain = true;
                                        transitModes.add("Train: " + lineName);
                                    }
                                }
                            }
                        }
                    }
                }

                routeInfo.put("has_bus", hasBus);
                routeInfo.put("has_train", hasTrain);
                routeInfo.put("has_subway", hasSubway);
                routeInfo.put("transit_modes", transitModes);
                routeInfo.put("route_data", route);

                // Add duration and distance
                if (route.has("legs") && route.get("legs").size() > 0) {
                    JsonNode leg = route.get("legs").get(0);
                    routeInfo.put("duration", leg.get("duration").get("text").asText());
                    routeInfo.put("distance", leg.get("distance").get("text").asText());
                }

                // Add fare if available
                if (route.has("fare")) {
                    routeInfo.put("fare", route.get("fare"));
                }

                routesWithModes.add(routeInfo);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("origin", origin);
        result.put("destination", destination);
        result.put("routes", routesWithModes);
        result.put("total_routes", routesWithModes.size());

        return result;
    }

    /**
     * Generic method to get directions - Simple version (backward compatible)
     */
    private JsonNode getDirections(String origin, String destination, String mode, boolean alternatives) {
        return getDirections(origin, destination, mode, alternatives, null);
    }

    /**
     * Generic method to get directions - Enhanced version with extra parameters
     */
    private JsonNode getDirections(String origin, String destination, String mode,
                                   boolean alternatives, Map<String, String> extraParams) {
//        logger.info("START :: getDirections(String origin : {}, String destination : {}, String mode : {}, boolean alternatives : {}, Map<String, String> extraParams : {}",
//                origin, destination, mode, alternatives, extraParams);
//        try {
            // Build base URL
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(DIRECTIONS_API_URL)
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .queryParam("mode", mode)
                    .queryParam("alternatives", alternatives)
                    .queryParam("key", apiKey);

            // Add extra parameters if provided
            if (extraParams != null && !extraParams.isEmpty()) {
                extraParams.forEach(builder::queryParam);
            }

            String url = builder.build().toUriString();

            logger.debug("Calling Google Maps API: {}", url.replace(apiKey, "***"));

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                throw new GoogleMapsException("Empty response from Google Maps API");
            }

            JsonNode jsonResponse = parseJson(response);

            // Validate response
            validateGoogleMapsResponse(jsonResponse);

            // Log route count if available
            if (jsonResponse.has("routes")) {
                int routeCount = jsonResponse.get("routes").size();
                logger.info("Received {} route(s) from {} to {}", routeCount, origin, destination);
            }

            return jsonResponse;

//        } catch (RestClientException e) {
//            logger.error("Failed to connect to Google Maps API", e);
//            throw new GoogleMapsException("Failed to connect to Google Maps API: " + e.getMessage(), e);
//        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
//            logger.error("Failed to parse JSON response", e);
//            throw new GoogleMapsException("Invalid JSON response from Google Maps API", e);
//        } catch (GoogleMapsException e) {
//            // Re-throw our custom exceptions
//            throw e;
//        } catch (Exception e) {
//            logger.error("Unexpected error processing Google Maps response", e);
//            throw new GoogleMapsException("Error processing Google Maps response: " + e.getMessage(), e);
//        }finally {
//            logger.info("END :: getDirections(String origin : {}, String destination : {}, String mode : {}, boolean alternatives : {}, Map<String, String> extraParams : {}",
//                    origin, destination, mode, alternatives, extraParams);
//        }
    }

    /**
     * Wrapper to convert checked JsonProcessingException to unchecked
     */
    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new GoogleMapsException("Failed to parse JSON response", e);
        }
    }

}
