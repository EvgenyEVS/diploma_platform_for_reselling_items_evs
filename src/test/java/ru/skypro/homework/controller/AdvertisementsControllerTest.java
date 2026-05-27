package ru.skypro.homework.controller;

import org.apache.tomcat.util.descriptor.web.InjectionTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.skypro.homework.IntegrationTestBase;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdvertisementsControllerTest extends IntegrationTestBase {

    private static final String USERNAME = "aduser@example.com";
    private static final String PASSWORD = "password123";
    private static final String ADMIN_USERNAME = "admin@example.com";
    private static final String ADMIN_PASSWORD = "admin123";

    @BeforeEach
    void setUp() throws Exception {
        // Create regular user
        Register user = new Register();
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setFirstName("User");
        user.setLastName("Test");
        user.setPhone("+7 123 456-78-90");
        user.setRole(Role.USER);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        // Create admin user
        Register admin = new Register();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(ADMIN_PASSWORD);
        admin.setFirstName("Admin");
        admin.setLastName("Adminov");
        admin.setPhone("+7 999 888-77-66");
        admin.setRole(Role.ADMIN);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllAds_ShouldReturnOk_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void addAd_ShouldReturnCreated_WhenAuthenticated() throws Exception {
        CreateOrUpdateAdDto adDto = new CreateOrUpdateAdDto();
        adDto.setTitle("Test Ad");
        adDto.setPrice(1000);
        adDto.setDescription("Test description");

        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                objectMapper.writeValueAsString(adDto).getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Ad"))
                .andExpect(jsonPath("$.price").value(1000));
    }

    @Test
    void addAd_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        CreateOrUpdateAdDto adDto = new CreateOrUpdateAdDto();
        adDto.setTitle("Test Ad");
        adDto.setPrice(1000);
        adDto.setDescription("Test description");

        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                objectMapper.writeValueAsString(adDto).getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAd_ShouldReturnAd_WhenExists() throws Exception {
        // First create an ad
        CreateOrUpdateAdDto adDto = new CreateOrUpdateAdDto();
        adDto.setTitle("Test Ad for Get");
        adDto.setPrice(2000);
        adDto.setDescription("Test description for get");

        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                objectMapper.writeValueAsString(adDto).getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        String response = mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int adId = objectMapper.readTree(response).get("pk").asInt();

        // Then get the ad
        mockMvc.perform(get("/ads/" + adId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Ad for Get"))
                .andExpect(jsonPath("$.price").value(2000))
                .andExpect(jsonPath("$.email").value(USERNAME));
    }

    @Test
    void deleteAd_UserCanDeleteOwnAd() throws Exception {
        // Create ad
        CreateOrUpdateAdDto adDto = new CreateOrUpdateAdDto();
        adDto.setTitle("Ad to Delete");
        adDto.setPrice(500);
        adDto.setDescription("Will be deleted");

        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                objectMapper.writeValueAsString(adDto).getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        String response = mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int adId = objectMapper.readTree(response).get("pk").asInt();

        // Delete ad
        mockMvc.perform(delete("/ads/" + adId)
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isNoContent());

        // Verify ad is deleted
        mockMvc.perform(get("/ads/" + adId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAd_AdminCanDeleteAnyAd() throws Exception {
        // Create ad as regular user
        CreateOrUpdateAdDto adDto = new CreateOrUpdateAdDto();
        adDto.setTitle("Admin Delete Test");
        adDto.setPrice(1000);
        adDto.setDescription("Admin will delete this");

        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                objectMapper.writeValueAsString(adDto).getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        String response = mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int adId = objectMapper.readTree(response).get("pk").asInt();

        // Delete as admin
        mockMvc.perform(delete("/ads/" + adId)
                        .with(httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMyAds_ShouldReturnUserAds() throws Exception {
        // Create ad
        CreateOrUpdateAdDto adDto = new CreateOrUpdateAdDto();
        adDto.setTitle("My Ad");
        adDto.setPrice(3000);
        adDto.setDescription("This is my ad");

        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                objectMapper.writeValueAsString(adDto).getBytes()
        );

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        mockMvc.perform(multipart("/ads")
                        .file(properties)
                        .file(image)
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        // Get my ads
        mockMvc.perform(get("/ads/me")
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].title").value("My Ad"));
    }

}



