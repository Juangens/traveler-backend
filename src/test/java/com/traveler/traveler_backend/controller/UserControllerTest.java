package com.traveler.traveler_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.traveler.traveler_backend.dto.CreateUserRequest;
import com.traveler.traveler_backend.dto.UpdateUserRequest;
import com.traveler.traveler_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "password123");

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("invalid-email", "password123");

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ShortPassword_ReturnsBadRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "short");

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_MissingFields_ReturnsBadRequest() throws Exception {
        // Given
        String invalidJson = "{}";

        // When/Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "password123");

        // Create first user
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        // When/Then - try to create duplicate
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("User with email test@example.com already exists"));
    }

    @Test
    void getUserById_ExistingUser_ReturnsOk() throws Exception {
        // Given - create a user first
        CreateUserRequest createRequest = new CreateUserRequest("test@example.com", "password123");
        String response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        // When/Then
        mockMvc.perform(get("/api/v1/users/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserById_NonExistingUser_ReturnsNotFound() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/v1/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    void getAllUsers_ReturnsOkWithList() throws Exception {
        // Given - create two users
        CreateUserRequest request1 = new CreateUserRequest("user1@example.com", "password123");
        CreateUserRequest request2 = new CreateUserRequest("user2@example.com", "password123");

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated());

        // When/Then
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].email").exists())
            .andExpect(jsonPath("$[1].email").exists());
    }

    @Test
    void updateUser_ValidRequest_ReturnsOk() throws Exception {
        // Given - create a user first
        CreateUserRequest createRequest = new CreateUserRequest("old@example.com", "password123");
        String response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        UpdateUserRequest updateRequest = new UpdateUserRequest("new@example.com", null);

        // When/Then
        mockMvc.perform(put("/api/v1/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void updateUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("invalid-email", null);

        // When/Then
        mockMvc.perform(put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_NonExistingUser_ReturnsNotFound() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("new@example.com", null);

        // When/Then
        mockMvc.perform(put("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // Given - create two users
        CreateUserRequest request1 = new CreateUserRequest("user1@example.com", "password123");
        CreateUserRequest request2 = new CreateUserRequest("user2@example.com", "password123");

        String response1 = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated());

        Long user1Id = objectMapper.readTree(response1).get("id").asLong();

        UpdateUserRequest updateRequest = new UpdateUserRequest("user2@example.com", null);

        // When/Then - try to update user1 with user2's email
        mockMvc.perform(put("/api/v1/users/" + user1Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_ExistingUser_ReturnsNoContent() throws Exception {
        // Given - create a user first
        CreateUserRequest createRequest = new CreateUserRequest("test@example.com", "password123");
        String response = mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        // When/Then
        mockMvc.perform(delete("/api/v1/users/" + userId))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_NonExistingUser_ReturnsNotFound() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/v1/users/999"))
            .andExpect(status().isNotFound());
    }
}
