package org.th.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.th.entity.User;
import org.th.entity.enums.Role;
import org.th.repository.UserRepository;
import org.th.request.LoginRequest;
import org.th.response.AuthResponse;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private String adminToken;

    @BeforeEach
    public void setup() throws Exception {
        // 1. Create a temporary Admin user
        String username = "test_admin_" + UUID.randomUUID().toString();
        String email = username + "@example.com";
        String rawPassword = "password123";

        adminUser = new User();
        adminUser.setUsername(username);
        adminUser.setEmail(email);
        adminUser.setPassword(passwordEncoder.encode(rawPassword));
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);

        adminUser = userRepository.save(adminUser);

        // 2. Login to get JWT
        LoginRequest loginRequest = new LoginRequest(username, rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseJson, AuthResponse.class);
        adminToken = authResponse.token();
    }

    @AfterEach
    public void cleanup() {
        if (adminUser != null && userRepository.existsById(adminUser.getId())) {
            userRepository.deleteById(adminUser.getId());
        }
    }

    @Test
    public void getDashboardStats_AsAdmin_ShouldReturn200_WithRealDB() throws Exception {
        // 3. Access Admin Endpoint with Token
        mockMvc.perform(get("/api/admin/dashboard/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").exists())
                .andExpect(jsonPath("$.data.totalShops").exists())
                .andExpect(jsonPath("$.data.totalReviews").exists());
    }

    @Test
    public void listUsers_AsAdmin_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
