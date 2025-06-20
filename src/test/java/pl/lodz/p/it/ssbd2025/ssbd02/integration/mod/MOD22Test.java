package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FeedbackTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FoodPyramidTestHelper;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOD22Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JavaMailSender mailSender;
    @Autowired
    private ObjectMapper objectMapper;
    private String token;

    @Autowired
    private FeedbackTestHelper  feedbackTestHelper;

    @Autowired
    private FoodPyramidTestHelper  foodPyramidTestHelper;

    private final String pyramidId = "00000000-0000-0000-0000-000000000011";
    private String feedbackId;

    @BeforeEach
    void setup() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        String loginRequestJson = """
                {
                  "login": "cfeta",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk()).andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        token = objectMapper.readTree(responseJson).get("value").asText();
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(delete("/api/mod/feedbacks/" + feedbackId)
                .header("Authorization", "Bearer " + token)).andReturn();

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    public void addFeedback() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        FoodPyramid foodPyramid1 = foodPyramidTestHelper.getFoodPyramid(UUID.fromString(pyramidId));

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600";
        int rating = 1;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        MvcResult result = mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.description").value(desc))
                .andExpect(jsonPath("$.rating").value(rating)).andReturn();

        String json = result.getResponse().getContentAsString();
        feedbackId = objectMapper.readTree(json).get("id").asText();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        Assertions.assertNotNull(feedbackTestHelper.getFeedback(UUID.fromString(feedbackId)));

        FoodPyramid foodPyramid2 = foodPyramidTestHelper.getFoodPyramid(UUID.fromString(pyramidId));

        Assertions.assertNotEquals(foodPyramid1.getAverageRating(), foodPyramid2.getAverageRating());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void addFeedbackMissingRating() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600";
        feedbackDTO.setDescription(desc);

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(feedbackDTO))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    // TODO: odkomentować jak będzie walidacja DTO
//    @Test
//    public void addFeedbackMissingDescription() throws Exception {
//        FeedbackDTO feedbackDTO = new FeedbackDTO();
//        int rating = 5;
//        feedbackDTO.setRating(rating);
//
//        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
//                        .header("Authorization", "Bearer " + token)
//                        .content(objectMapper.writeValueAsString(feedbackDTO))
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    public void addFeedbackTooLowRating() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600";
        int rating = -5;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(feedbackDTO))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void addFeedbackTooHighRating() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600";
        int rating = 25;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addFeedbackNotYourPyramid() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600";
        int rating = 5;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        String notYouPyramidId = "00000000-0000-0000-0000-000000000012";

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + notYouPyramidId)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void addFeedbackAlreadyRated() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600";
        int rating = 5;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        MvcResult result = mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.description").value(desc))
                .andExpect(jsonPath("$.rating").value(rating)).andReturn();

        String json = result.getResponse().getContentAsString();
        feedbackId = objectMapper.readTree(json).get("id").asText();

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void addFeedbackPyramidNotExists() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600";
        int rating = 5;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        String notExistPyramidId = "00000000-0000-2137-0000-000000000012";

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + notExistPyramidId)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addFeedbackInvalidUUID() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600";
        int rating = 5;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        String invalidUUID = "invalidUUID";

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + invalidUUID)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addFeedbackTooLongDescription() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "Fajny piramidka pozdro 600fdnjsaklnvcjxkzlnvjdskalnvdjksalnvdjskalnvjsdklanvdjk" +
                "slnvjsdkavnsdjkvnsdjkvnsdkjvnasdjkvnsdjklvnsadjklvnsdkjlvnsdajkvsdnklvjasdnvjkasdnvkjlsd" +
                "nvkjlasdnvjkasdvnksdjvnasdjklvnsdklvnsdklvnkasdlhdfshdfshsdfhsdfhsdfhsdfhsdfhdsfhsdfhdfsh" +
                "dfshsdfhdfshdfshdfshdfshdfshsdfhdfshdfshdfshdfshdfshdfshdfshdfshdfhfdshhfdshdfshsdfhdfhdfshdf";
        int rating = 5;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addFeedbackTooShortDescription() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        String desc = "";
        int rating = 5;
        feedbackDTO.setDescription(desc);
        feedbackDTO.setRating(rating);

        mockMvc.perform(post("/api/mod/feedbacks/pyramid/" + pyramidId)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}