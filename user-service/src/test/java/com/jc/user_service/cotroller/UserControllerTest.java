// src/test/java/com/example/userservice/controller/UserControllerTest.java
package com.jc.user_service.cotroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jc.user_service.controller.UserController;
import com.jc.user_service.dto.UserRequest;
import com.jc.user_service.exception.UserNotFoundException;
import com.jc.user_service.model.User;
import com.jc.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------- CREATE ----------------
    @Test
    void shouldReturnValidationErrors_whenInvalidUserData() throws Exception {
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setName("a");
        invalidRequest.setEmail("invalid");
        invalidRequest.setPassword("123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name should be between 2 and 50 characters"))
                .andExpect(jsonPath("$.email").value("Email should be valid"));
    }

    @Test
    void shouldCreateUserSuccessfully_whenValidData() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "secret123");
        User user = new User(1L, "Alice", "alice@example.com", "secret123");

        Mockito.when(userService.createUser(any(UserRequest.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    // ---------------- READ ----------------
    @Test
    void shouldReturnUserNotFound_whenUserDoesNotExist() throws Exception {
        Mockito.when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID: 99 not found"));
    }

    // ---------------- UPDATE ----------------
    @Test
    void shouldUpdateUserSuccessfully_whenValidData() throws Exception {
        UserRequest request = new UserRequest("Bob", "bob@example.com", "mypassword");
        User updatedUser = new User(1L, "Bob", "bob@example.com", "mypassword");

        Mockito.when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bob"))
                .andExpect(jsonPath("$.email").value("bob@example.com"));
    }

    @Test
    void shouldReturnNotFound_whenUpdatingNonExistentUser() throws Exception {
        UserRequest request = new UserRequest("Ghost", "ghost@example.com", "ghostpass");

        Mockito.when(userService.updateUser(eq(99L), any(UserRequest.class))).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(put("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID: 99 not found"));
    }

    // ---------------- DELETE ----------------
    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFound_whenDeletingNonExistentUser() throws Exception {
        Mockito.doThrow(new UserNotFoundException(99L)).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID: 99 not found"));
    }

    // ---------------- GENERIC ERROR ----------------
    @Test
    void shouldReturnInternalServerError_whenUnexpectedExceptionOccurs() throws Exception {
        Mockito.when(userService.getAllUsers()).thenThrow(new RuntimeException("DB is down"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Something went wrong: DB is down"));
    }
}
