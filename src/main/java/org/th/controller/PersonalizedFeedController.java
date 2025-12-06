package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.feed.PersonalizedFeedDTO;
import org.th.service.PersonalizedFeedService;

/**
 * REST controller for personalized feed endpoints
 */
@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Tag(name = "Personalized Feed", description = "Behavior-based personalized recommendations")
public class PersonalizedFeedController {

        private final PersonalizedFeedService personalizedFeedService;

        /**
         * Get complete personalized feed for authenticated user
         */
        @GetMapping("/personalized")
        @Operation(summary = "Get personalized feed", description = "Returns a personalized feed with multiple sections based on user behavior, time context, and location")
        public ResponseEntity<ApiResponse<PersonalizedFeedDTO>> getPersonalizedFeed(
                        Authentication authentication,
                        @Parameter(description = "User's current latitude (optional)") @RequestParam(required = false) Double latitude,
                        @Parameter(description = "User's current longitude (optional)") @RequestParam(required = false) Double longitude,
                        @Parameter(description = "Search radius in kilometers (default: 5)") @RequestParam(required = false, defaultValue = "5.0") Double radiusKm) {
                String username = authentication.getName();

                PersonalizedFeedDTO feed = personalizedFeedService.generatePersonalizedFeed(
                                username, latitude, longitude, radiusKm);

                return ResponseEntity.ok(
                                ApiResponse.success("Personalized feed generated successfully", feed));
        }

        /**
         * Get personalized feed without authentication (for guest users)
         */
        @GetMapping("/guest")
        @Operation(summary = "Get guest feed", description = "Returns a generic feed for non-authenticated users based on trending and location")
        public ResponseEntity<ApiResponse<PersonalizedFeedDTO>> getGuestFeed(
                        @Parameter(description = "User's current latitude (optional)") @RequestParam(required = false) Double latitude,
                        @Parameter(description = "User's current longitude (optional)") @RequestParam(required = false) Double longitude,
                        @Parameter(description = "Search radius in kilometers (default: 5)") @RequestParam(required = false, defaultValue = "5.0") Double radiusKm) {
                // For guest users, use a default/guest username or create a simplified version
                // This is a placeholder - you might want to create a separate method for guests
                PersonalizedFeedDTO feed = personalizedFeedService.generatePersonalizedFeed(
                                "guest", latitude, longitude, radiusKm);

                return ResponseEntity.ok(
                                ApiResponse.success("Guest feed generated successfully", feed));
        }
}
