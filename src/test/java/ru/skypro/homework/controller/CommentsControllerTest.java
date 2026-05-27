package ru.skypro.homework.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.skypro.homework.IntegrationTestBase;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.ads.CreateOrUpdateAdDto;
import ru.skypro.homework.dto.comments.CreateOrUpdateCommentDto;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CommentsControllerTest extends IntegrationTestBase {

    private static final String USERNAME = "commentuser@example.com";
    private static final String PASSWORD = "password123";
    private static final String OTHER_USER = "other@example.com";
    private static final String OTHER_PASSWORD = "other123";
    private static final String ADMIN_USERNAME = "admin@example.com";
    private static final String ADMIN_PASSWORD = "admin123";

    private int testAdId;

    @BeforeEach
    void setUp() throws Exception {
        // Create users
        Register user = new Register();
        user.setUsername(USERNAME);
        user.setPassword(PASSWORD);
        user.setFirstName("Comment");
        user.setLastName("User");
        user.setPhone("+7 123 456-78-90");
        user.setRole(Role.USER);
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        Register other = new Register();
        other.setUsername(OTHER_USER);
        other.setPassword(OTHER_PASSWORD);
        other.setFirstName("Other");
        other.setLastName("User");
        other.setPhone("+7 999 888-77-66");
        other.setRole(Role.USER);
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(other)))
                .andExpect(status().isCreated());

        Register admin = new Register();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(ADMIN_PASSWORD);
        admin.setFirstName("Admin");
        admin.setLastName("Adminov");
        admin.setPhone("+7 777 777-77-77");
        admin.setRole(Role.ADMIN);
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(admin)))
                .andExpect(status().isCreated());

        // Create test ad
        CreateOrUpdateAdDto adDto = new CreateOrUpdateAdDto();
        adDto.setTitle("Ad for Comments");
        adDto.setPrice(1000);
        adDto.setDescription("Test ad for comments");

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

        testAdId = objectMapper.readTree(response).get("pk").asInt();
    }

    @Test
    void getComments_ShouldReturnComments_WhenAdExists() throws Exception {
        mockMvc.perform(get("/ads/" + testAdId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").exists())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    void addComment_ShouldReturnCreated_WhenValid() throws Exception {
        CreateOrUpdateCommentDto comment = new CreateOrUpdateCommentDto();
        comment.setText("This is a test comment");

        mockMvc.perform(post("/ads/" + testAdId + "/comments")
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("This is a test comment"))
                .andExpect(jsonPath("$.authorFirstName").value("Comment"));
    }

    @Test
    void addComment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        CreateOrUpdateCommentDto comment = new CreateOrUpdateCommentDto();
        comment.setText("This comment should not be added");

        mockMvc.perform(post("/ads/" + testAdId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateComment_UserCanUpdateOwnComment() throws Exception {
        // Create comment
        CreateOrUpdateCommentDto createComment = new CreateOrUpdateCommentDto();
        createComment.setText("Original comment");

        String response = mockMvc.perform(post("/ads/" + testAdId + "/comments")
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createComment)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int commentId = objectMapper.readTree(response).get("pk").asInt();

        // Update comment
        CreateOrUpdateCommentDto updateComment = new CreateOrUpdateCommentDto();
        updateComment.setText("Updated comment text");

        mockMvc.perform(patch("/ads/" + testAdId + "/comments/" + commentId)
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated comment text"));
    }

    @Test
    void updateComment_UserCannotUpdateOtherComment() throws Exception {
        // Create comment as first user
        CreateOrUpdateCommentDto createComment = new CreateOrUpdateCommentDto();
        createComment.setText("Comment by first user");

        String response = mockMvc.perform(post("/ads/" + testAdId + "/comments")
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createComment)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int commentId = objectMapper.readTree(response).get("pk").asInt();

        // Try to update as other user
        CreateOrUpdateCommentDto updateComment = new CreateOrUpdateCommentDto();
        updateComment.setText("Attempt to hack");

        mockMvc.perform(patch("/ads/" + testAdId + "/comments/" + commentId)
                        .with(httpBasic(OTHER_USER, OTHER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateComment)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteComment_UserCanDeleteOwnComment() throws Exception {
        // Create comment
        CreateOrUpdateCommentDto createComment = new CreateOrUpdateCommentDto();
        createComment.setText("Comment to delete");

        String response = mockMvc.perform(post("/ads/" + testAdId + "/comments")
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createComment)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int commentId = objectMapper.readTree(response).get("pk").asInt();

        // Delete comment
        mockMvc.perform(delete("/ads/" + testAdId + "/comments/" + commentId)
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteComment_AdminCanDeleteAnyComment() throws Exception {
        // Create comment as user
        CreateOrUpdateCommentDto createComment = new CreateOrUpdateCommentDto();
        createComment.setText("Admin will delete this");

        String response = mockMvc.perform(post("/ads/" + testAdId + "/comments")
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createComment)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int commentId = objectMapper.readTree(response).get("pk").asInt();

        // Delete as admin
        mockMvc.perform(delete("/ads/" + testAdId + "/comments/" + commentId)
                        .with(httpBasic(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());
    }

}
