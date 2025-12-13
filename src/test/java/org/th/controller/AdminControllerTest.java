package org.th.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.th.config.security.JwtAuthenticationFilter;
import org.th.config.security.RateLimitingFilter;
import org.th.config.security.SecurityConfig;
import org.th.repository.ShopRepository;
import org.th.repository.UserRepository;
import org.th.service.admin.AdminService;
import org.th.controller.admin.AdminController;
import org.th.service.ShopService;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ShopRepository shopRepository;

    @MockBean
    private ShopService shopService;

    // Security Deps
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    @org.junit.jupiter.api.BeforeEach
    public void setup() throws Exception {
        // Ensure the filter chain continues even when filters are mocked
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());

        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(rateLimitingFilter).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void getDashboardStats_AsAdmin_ShouldReturn200() throws Exception {
        when(adminService.getSystemStats()).thenReturn(Map.of("users", 100));

        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void getDashboardStats_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getDashboardStats_Unauthenticated_ShouldReturn403() throws Exception {
        // Fallback to 403 or 401 depending on how SecurityConfig is set up for
        // anonymous
        // In this config, it typically redirects or returns 401/403
        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isForbidden());
    }
}
