package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.exception.UsernameAlreadyExistsException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_ShouldSaveUser_WhenValidData() {
        Register register = new Register();
        register.setUsername("test@example.com");
        register.setPassword("password123");
        register.setFirstName("John");
        register.setLastName("Doe");
        register.setPhone("+7 123 456-78-90");
        register.setRole(Role.USER);

        User user = new User();
        user.setUsername("test@example.com");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(register)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = authService.register(register);

        assertThat(result).isTrue();
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_ShouldSetDefaultRole_WhenRoleIsNull() {
        Register register = new Register();
        register.setUsername("test@example.com");
        register.setPassword("password123");
        register.setFirstName("John");
        register.setLastName("Doe");
        register.setPhone("+7 123 456-78-90");
        register.setRole(null);

        User user = new User();
        user.setUsername("test@example.com");

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(register)).thenReturn(user);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = authService.register(register);

        assertThat(result).isTrue();
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRole() == Role.USER
        ));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        Register register = new Register();
        register.setUsername("existing@example.com");
        register.setPassword("password123");
        register.setFirstName("John");
        register.setLastName("Doe");
        register.setPhone("+7 123 456-78-90");
        register.setRole(Role.USER);

        User existingUser = new User();
        existingUser.setUsername("existing@example.com");

        when(userRepository.findByUsername("existing@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(register))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnTrue_WhenValidCredentials() {
        String username = "test@example.com";
        String password = "password123";

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);

        boolean result = authService.login(username, password);

        assertThat(result).isTrue();
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, "encodedPassword");
    }

    @Test
    void login_ShouldReturnFalse_WhenInvalidPassword() {
        String username = "test@example.com";
        String password = "wrongPassword";

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(false);

        boolean result = authService.login(username, password);

        assertThat(result).isFalse();
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, "encodedPassword");
    }

    @Test
    void login_ShouldReturnFalse_WhenUserNotFound() {
        String username = "nonexistent@example.com";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        boolean result = authService.login(username, password);

        assertThat(result).isFalse();
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        String username = "test@example.com";

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails userDetails = authService.loadUserByUsername(username);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        String username = "nonexistent@example.com";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}