package org.th.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.th.dto.ApiResponse;
import org.th.dto.UserProfileDTO;
import org.th.entity.User;
import org.th.repository.UserRepository;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile and preferences")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user's profile and taste preferences")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setIsVegetarian(user.getIsVegetarian());
        dto.setIsHalal(user.getIsHalal());
        dto.setPricePreference(user.getPricePreference());
        if (user.getPricePreference() != null) {
            dto.setPricePreferenceMm(user.getPricePreference().getLabelMm());
        }
        dto.setSpicinessPreference(user.getSpicinessPreference());
        if (user.getSpicinessPreference() != null) {
            dto.setSpicinessPreferenceMm(user.getSpicinessPreference().getLabelMm());
        }

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", dto));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update taste preferences")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateProfile(@RequestBody UserProfileDTO request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getIsVegetarian() != null)
            user.setIsVegetarian(request.getIsVegetarian());
        if (request.getIsHalal() != null)
            user.setIsHalal(request.getIsHalal());
        if (request.getPricePreference() != null)
            user.setPricePreference(request.getPricePreference());
        if (request.getSpicinessPreference() != null)
            user.setSpicinessPreference(request.getSpicinessPreference());

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Profile updated", request));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
