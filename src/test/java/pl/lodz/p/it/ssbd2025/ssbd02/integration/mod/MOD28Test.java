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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.FeedbackNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FeedbackTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FoodPyramidTestHelper;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertThrows;
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
public class MOD28Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JavaMailSender mailSender;
    @Autowired
    private ObjectMapper objectMapper;
    private String token;

    private String feedbackId;

    private final String pyramidId = "00000000-0000-0000-0000-000000000011";

    @Autowired
    private FeedbackTestHelper feedbackTestHelper;

    @Autowired
    private FoodPyramidTestHelper foodPyramidTestHelper;

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
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        token = objectMapper.readTree(responseJson).get("value").asText();

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
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(delete("/api/mod/feedbacks/" + feedbackId)
                .header("Authorization", "Bearer " + token)).andReturn();

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void deleteFeedback() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        FoodPyramid foodPyramid1 = foodPyramidTestHelper.getFoodPyramid(UUID.fromString(pyramidId));

        mockMvc.perform(delete("/api/mod/feedbacks/" + feedbackId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        assertThrows(FeedbackNotFoundException.class, () -> {
            feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));
        });

        FoodPyramid foodPyramid2 = foodPyramidTestHelper.getFoodPyramid(UUID.fromString(pyramidId));

        Assertions.assertNotEquals(foodPyramid1.getAverageRating(), foodPyramid2.getAverageRating());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void deleteFeedbackInvalidUUID() throws Exception {
        String invalidUUID = "invalidUUID";

        mockMvc.perform(delete("/api/mod/feedbacks/" + invalidUUID)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteFeedbackNotExistingFeedback() throws Exception {
        String invalidUUID = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/mod/feedbacks/" + invalidUUID)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteNotYourFeedback() throws Exception {
        String loginRequestJson = """
                {
                  "login": "agorgonzola",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token2 = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(delete("/api/mod/feedbacks/" + feedbackId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }
}
