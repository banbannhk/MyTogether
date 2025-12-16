package org.th.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.UserDTO;
import org.th.entity.User;
import org.th.repository.ShopRepository;
import org.th.repository.ShopReviewRepository;
import org.th.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ShopReviewRepository shopReviewRepository;

    /**
     * Get system-wide statistics for the admin dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStats() {
        log.info("Fetching system statistics for admin dashboard");

        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalShops = shopRepository.count();
        long totalReviews = shopReviewRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalShops", totalShops);
        stats.put("totalReviews", totalReviews);

        return stats;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;
        if (search != null && !search.isEmpty()) {
            users = userRepository.searchUsers(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::mapToUserDTO);
    }

    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName()); // Assuming fullName exists in User
        dto.setEnabled(user.isEnabled());
        // Simple role mapping logic
        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        return dto;
    }
}
