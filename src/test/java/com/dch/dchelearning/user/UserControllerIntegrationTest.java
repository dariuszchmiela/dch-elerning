package com.dch.dchelearning.user;

import com.dch.dchelearning.IntegrationTest;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.notNullValue;

@IntegrationTest
class UserControllerIntegrationTest {

    private static final String REGISTER_PATH = "/api/users/register";
    private static final String LOGIN_PATH = "/api/users/login";
    private static final String ME_PATH = "/api/users/me";

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

        mockMvc.perform(post(REGISTER_PATH)
                .contentType("application/json")
                .content(registerBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(email));

        String loginBody = objectMapper.writeValueAsString(new LoginRequest(email, password));

        mockMvc.perform(post(LOGIN_PATH)
                .contentType("application/json")
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    void meShouldReturnCurrentUserWhenAuthenticated() throws Exception {
        String email = "me@example.com";
        String password = "securePassword123";

        String registerBody = objectMapper.writeValueAsString(new RegisterUserRequest(email, password, "STUDENT"));
        mockMvc.perform(post(REGISTER_PATH)
                .contentType("application/json")
                .content(registerBody))
            .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(new LoginRequest(email, password));
        MvcResult loginResult = mockMvc.perform(post(LOGIN_PATH)
                .contentType("application/json")
                .content(loginBody))
            .andExpect(status().isOk())
            .andReturn();

        String token = extractToken(loginResult);

        mockMvc.perform(get(ME_PATH)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email));
    }

    private String extractToken(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        return json.get("token").asText();
    }
}