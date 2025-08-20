package com.jc.user_service.service;

import com.jc.user_service.dto.UserRequest;
import com.jc.user_service.exception.UserNotFoundException;
import com.jc.user_service.model.User;
import com.jc.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        request = new UserRequest("Alice", "alice@example.com", "secret123");
        user = new User(1L, "Alice", "alice@example.com", "secret123");
    }


    @Test
    void shouldCreateUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.createUser(request);

        assertEquals(1L, savedUser.getId());
        assertEquals("Alice", savedUser.getName());
        assertEquals("alice@example.com", savedUser.getEmail());

        // Capture what was saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("secret123", userCaptor.getValue().getPassword());
    }

    @Test
    void shouldReturnUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getUserById(1L);

        assertEquals("Alice", found.getName());
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L), "User with ID: 99 not found");
    }

    @Test
    void shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("alice@example.com", users.getFirst().getEmail());
    }

    @Test
    void shouldUpdateUser() {
        UserRequest updateRequest = new UserRequest("Bob", "bob@example.com", "newpass");
        User updatedUser = new User(1L, "Bob", "bob@example.com", "newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(1L, updateRequest);

        assertEquals("Bob", result.getName());
        assertEquals("bob@example.com", result.getEmail());

    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserRequest updateRequest = new UserRequest("Ghost", "ghost@example.com", "ghostpass");

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, updateRequest), "User with ID: 99 not found");
    }

    @Test
    void shouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(1L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).delete(captor.capture());

        assertEquals(1L, captor.getValue().getId());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L), "User with ID 99 not found");
    }


}
