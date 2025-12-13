package org.th.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
    // private LocalDateTime lastLogin; // Not in Entity
    // private String profileImage; // Not in Entity
}
