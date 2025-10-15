package org.th.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RouteDetailsHelper {
    String routeType;
    String transitMode;
    Double distanceKm;
    Integer durationMinutes;
    Double fareAmount;

    public RouteDetailsHelper() {
    }

    /**
     * Extract route details helper
     */
    public RouteDetailsHelper(JsonNode routeData) {

        if (routeData.has("routes") && !routeData.get("routes").isEmpty()) {
            JsonNode route = routeData.get("routes").get(0);

            if (route.has("legs") && !route.get("legs").isEmpty()) {
                JsonNode leg = route.get("legs").get(0);

                // Extract distance
                if (leg.has("distance")) {
                    this.distanceKm = leg.get("distance").get("value").asDouble() / 1000.0;
                }

                // Extract duration
                if (leg.has("duration")) {
                    this.durationMinutes = leg.get("duration").get("value").asInt() / 60;
                }

                // Extract transit mode
                if (leg.has("steps")) {
                    for (JsonNode step : leg.get("steps")) {
                        if (step.has("transit_details")) {
                            JsonNode vehicle = step.get("transit_details").get("line").get("vehicle");
                            this.transitMode = vehicle.get("type").asText();
                            break;
                        }
                    }
                }
            }

            // Extract fare
            if (route.has("fare")) {
                this.fareAmount = route.get("fare").get("value").asDouble();
            }
        }
    }
}
