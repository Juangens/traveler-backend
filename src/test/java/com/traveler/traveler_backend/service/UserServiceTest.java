package com.traveler.traveler_backend.service;

import com.traveler.traveler_backend.dto.CreateUserRequest;
import com.traveler.traveler_backend.dto.UpdateUserRequest;
import com.traveler.traveler_backend.dto.UserResponse;
import com.traveler.traveler_backend.entity.User;
import com.traveler.traveler_backend.exception.DuplicateEmailException;
import com.traveler.traveler_backend.exception.UserNotFoundException;
import com.traveler.traveler_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success_ReturnsUserResponse() {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "password123");
        User savedUser = createMockUser(1L, "test@example.com", "hashedPassword");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsDuplicateEmailException() {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "password123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("test@example.com");

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_HashesPassword() {
        // Given
        CreateUserRequest request = new CreateUserRequest("test@example.com", "plainPassword");
        User savedUser = createMockUser(1L, "test@example.com", "hashedPassword");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        userService.createUser(request);

        // Then
        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    void getUserById_ExistingUser_ReturnsUserResponse() {
        // Given
        User user = createMockUser(1L, "test@example.com", "hashedPassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUserById(1L);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NonExistingUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("999");

        verify(userRepository).findById(999L);
    }

    @Test
    void getAllUsers_ReturnsListOfUsers() {
        // Given
        User user1 = createMockUser(1L, "user1@example.com", "hash1");
        User user2 = createMockUser(2L, "user2@example.com", "hash2");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // When
        List<UserResponse> responses = userService.getAllUsers();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).email()).isEqualTo("user1@example.com");
        assertThat(responses.get(1).email()).isEqualTo("user2@example.com");
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_EmptyDatabase_ReturnsEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserResponse> responses = userService.getAllUsers();

        // Then
        assertThat(responses).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_EmailOnly_Success() {
        // Given
        User existingUser = createMockUser(1L, "old@example.com", "hashedPassword");
        UpdateUserRequest request = new UpdateUserRequest("new@example.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        UserResponse response = userService.updateUser(1L, request);

        // Then
        assertThat(response.email()).isEqualTo("new@example.com");
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(existingUser);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_PasswordOnly_Success() {
        // Given
        User existingUser = createMockUser(1L, "test@example.com", "oldHash");
        UpdateUserRequest request = new UpdateUserRequest(null, "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.updateUser(1L, request);

        // Then
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(existingUser);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_BothFields_Success() {
        // Given
        User existingUser = createMockUser(1L, "old@example.com", "oldHash");
        UpdateUserRequest request = new UpdateUserRequest("new@example.com", "newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.updateUser(1L, request);

        // Then
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_DuplicateEmail_ThrowsDuplicateEmailException() {
        // Given
        User existingUser = createMockUser(1L, "old@example.com", "hashedPassword");
        UpdateUserRequest request = new UpdateUserRequest("taken@example.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.updateUser(1L, request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("taken@example.com");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_NonExistingUser_ThrowsUserNotFoundException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("new@example.com", null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.updateUser(999L, request))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("999");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_HashesNewPassword() {
        // Given
        User existingUser = createMockUser(1L, "test@example.com", "oldHash");
        UpdateUserRequest request = new UpdateUserRequest(null, "newPlainPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPlainPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.updateUser(1L, request);

        // Then
        verify(passwordEncoder).encode("newPlainPassword");
    }

    @Test
    void deleteUser_ExistingUser_Success() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistingUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("999");

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    // Helper method to create mock users
    private User createMockUser(Long id, String email, String passwordHash) {
        User user = new User(email, passwordHash);
        user.setId(id);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
