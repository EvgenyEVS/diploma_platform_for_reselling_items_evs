package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.user.NewPasswordDto;
import ru.skypro.homework.dto.user.UpdateUserDto;
import ru.skypro.homework.dto.user.UserDto;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findByUsername_ShouldReturnUser_WhenExists() {
        String username = "test@example.com";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = userService.findByUsername(username);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    void findByUsername_ShouldThrowNotFound_WhenNotExists() {
        String username = "nonexistent@example.com";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername(username))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUser_ShouldReturnUserDto_WhenUserExists() {
        String username = "test@example.com";
        User user = new User();
        user.setUsername(username);
        user.setFirstName("John");
        user.setLastName("Doe");

        UserDto userDto = new UserDto();
        userDto.setEmail(username);
        userDto.setFirstName("John");
        userDto.setLastName("Doe");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getUser(username);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(username);
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void updateUserPassword_ShouldUpdatePassword_WhenValidCredentials() {
        String username = "test@example.com";
        NewPasswordDto newPasswordDto = new NewPasswordDto();
        newPasswordDto.setCurrentPassword("oldPassword");
        newPasswordDto.setNewPassword("newPassword");

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedOldPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUserPassword(username, newPasswordDto);

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals("encodedNewPassword")
        ));
    }

    @Test
    void updateUserPassword_ShouldThrowForbidden_WhenWrongCurrentPassword() {
        String username = "test@example.com";
        NewPasswordDto newPasswordDto = new NewPasswordDto();
        newPasswordDto.setCurrentPassword("wrongPassword");
        newPasswordDto.setNewPassword("newPassword");

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedOldPassword");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.updateUserPassword(username, newPasswordDto))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldUpdateUserInfo() {
        String username = "test@example.com";
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setFirstName("UpdatedName");
        updateUserDto.setLastName("UpdatedLastName");
        updateUserDto.setPhone("+7 999 888-77-66");

        User user = new User();
        user.setUsername(username);
        user.setFirstName("OldName");
        user.setLastName("OldLastName");
        user.setPhone("+7 123 456-78-90");

        UpdateUserDto expectedDto = new UpdateUserDto();
        expectedDto.setFirstName("UpdatedName");
        expectedDto.setLastName("UpdatedLastName");
        expectedDto.setPhone("+7 999 888-77-66");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUpdateUserDto(user)).thenReturn(expectedDto);

        UpdateUserDto result = userService.updateUser(username, updateUserDto);

        assertThat(result.getFirstName()).isEqualTo("UpdatedName");
        assertThat(result.getLastName()).isEqualTo("UpdatedLastName");
        assertThat(result.getPhone()).isEqualTo("+7 999 888-77-66");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_ShouldNotUpdateNullFields() {
        String username = "test@example.com";
        UpdateUserDto updateUserDto = new UpdateUserDto();
        updateUserDto.setFirstName("UpdatedName");

        User user = new User();
        user.setUsername(username);
        user.setFirstName("OldName");
        user.setLastName("OldLastName");
        user.setPhone("+7 123 456-78-90");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUpdateUserDto(any())).thenReturn(new UpdateUserDto());

        userService.updateUser(username, updateUserDto);

        assertThat(user.getFirstName()).isEqualTo("UpdatedName");
        assertThat(user.getLastName()).isEqualTo("OldLastName");
        assertThat(user.getPhone()).isEqualTo("+7 123 456-78-90");
    }

    @Test
    void updateUserImage_ShouldSaveImage_WhenValid() throws IOException {
        String username = "test@example.com";
        byte[] imageBytes = "test image content".getBytes();

        User user = new User();
        user.setUsername(username);
        user.setImage(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.jpg");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(imageBytes));
        when(userRepository.save(any(User.class))).thenReturn(user);

        String result = userService.updateUserImage(username, multipartFile);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserImage_ShouldReturnEmptyArray_WhenUserHasNoImage() {
        String username = "test@example.com";
        User user = new User();
        user.setUsername(username);
        user.setImage(null);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        byte[] result = userService.getUserImage(username);

        assertThat(result).isEmpty();
    }
}