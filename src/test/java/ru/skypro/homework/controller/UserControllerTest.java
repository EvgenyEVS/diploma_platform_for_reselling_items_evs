package ru.skypro.homework.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.skypro.homework.IntegrationTestBase;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.user.NewPasswordDto;
import ru.skypro.homework.dto.user.UpdateUserDto;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends IntegrationTestBase {

    private static final String TEST_USERNAME = "user@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() throws Exception {
        Register register = new Register();
        register.setUsername(TEST_USERNAME);
        register.setPassword(TEST_PASSWORD);
        register.setFirstName("John");
        register.setLastName("Doe");
        register.setPhone("+7 123 456-78-90");
        register.setRole(Role.USER);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());
    }

    @Test
    void getUser_ShouldReturnUserInfo_WhenAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me")
                        .with(httpBasic(TEST_USERNAME, TEST_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_USERNAME))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getUser_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setPassword_ShouldUpdatePassword_WhenValidCredentials() throws Exception {
        NewPasswordDto newPassword = new NewPasswordDto();
        newPassword.setCurrentPassword(TEST_PASSWORD);
        newPassword.setNewPassword("newPassword456");

        mockMvc.perform(post("/users/set_password")
                        .with(httpBasic(TEST_USERNAME, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassword)))
                .andExpect(status().isOk());

        // Verify can login with new password
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + TEST_USERNAME + "\",\"password\":\"newPassword456\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void setPassword_ShouldReturnForbidden_WhenWrongCurrentPassword() throws Exception {
        NewPasswordDto newPassword = new NewPasswordDto();
        newPassword.setCurrentPassword("wrongpassword");
        newPassword.setNewPassword("newPassword456");

        mockMvc.perform(post("/users/set_password")
                        .with(httpBasic(TEST_USERNAME, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassword)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_ShouldUpdateUserInfo() throws Exception {
        UpdateUserDto updateUser = new UpdateUserDto();
        updateUser.setFirstName("Updated");
        updateUser.setLastName("Name");
        updateUser.setPhone("+7 999 888-77-66");

        mockMvc.perform(patch("/users/me")
                        .with(httpBasic(TEST_USERNAME, TEST_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

    @Test
    void updateUserImage_ShouldUpdateAvatar() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/users/me/image")
                        .file(image)
                        .with(httpBasic(TEST_USERNAME, TEST_PASSWORD))
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

}
