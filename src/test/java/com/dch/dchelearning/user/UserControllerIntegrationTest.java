package com.dch.dchelearning.user;

import com.dch.dchelearning.IntegrationTest;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.notNullValue;

@IntegrationTest
class UserControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void registerThenLoginShouldReturnToken() throws Exception {
        String email = "integration@example.com";
        String password = "securePassword123";

        String registerBody = objectMapper.writeValueAsString(new RegisterUserRequest(email, password, "STUDENT"));

        mockMvc.perform(post("/api/users/register")
                .contentType("application/json")
                .content(registerBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(email));

        String loginBody = objectMapper.writeValueAsString(new LoginRequest(email, password));

        mockMvc.perform(post("/api/users/login")
                .contentType("application/json")
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()));
    }
}