package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FeedbackTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FoodPyramidTestHelper;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
public class MOD9Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackTestHelper feedbackTestHelper;

    @Autowired
    private FoodPyramidTestHelper foodPyramidTestHelper;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID foodPyramidId;
    private UUID feedbackId;
    private UUID existingFoodPyramidId;
    private UUID clientId;
    private String clientToken;

    private String dieticianToken;

    private final String clientLogin = "agorgonzola";
    private final String dieticianLogin = "tcheese";

    private String loginAndGetToken(String login, String password) throws Exception {
        String loginJson = """
                {
                  "login": "%s",
                  "password": "%s"
                }
                """.formatted(login, password);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("value").asText();
    }

    @BeforeEach
    public void setup() throws Exception {
        clientToken = loginAndGetToken(clientLogin, "P@ssw0rd!");
        dieticianToken = loginAndGetToken(dieticianLogin, "P@ssw0rd!");

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));

        var authentication = new UsernamePasswordAuthenticationToken(clientLogin, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        FoodPyramid foodPyramid = foodPyramidTestHelper.getExistingFoodPyramid();
        foodPyramidId = foodPyramid.getId();

        existingFoodPyramidId = foodPyramidTestHelper.getExistingFoodPyramid().getId();

        Feedback feedback = feedbackTestHelper.getFeedbackByFoodPyramidId(foodPyramidId).get(0);
        feedbackId = feedback.getId();
        clientId = feedback.getClient().getId();
    }

    @AfterEach
    public void teardown() throws Exception {
        if (clientToken != null) {
            mockMvc.perform(post("/api/account/logout")
                            .header("Authorization", "Bearer " + clientToken))
                    .andReturn();
            clientToken = null;
        }
        if (dieticianToken != null) {
            mockMvc.perform(post("/api/account/logout")
                            .header("Authorization", "Bearer " + dieticianToken))
                    .andReturn();
            dieticianToken = null;
        }
        SecurityContextHolder.clearContext();
    }


    @Test
    public void testGetMyFeedbackForPyramid() throws Exception {
        mockMvc.perform(get("/api/mod/feedbacks/my-pyramid/" + foodPyramidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(feedbackId.toString()))
                .andExpect(jsonPath("$.rating").value(2))
                .andExpect(jsonPath("$.description").value("Średnio widze efekty po tym profilu żywieniowym"))
                .andExpect(jsonPath("$.lockToken").exists());
    }

    @Test
    public void testGetFeedbackByClientAndPyramid_NotFound() throws Exception {
        UUID nonExistentPyramid = UUID.randomUUID();
        mockMvc.perform(get("/api/mod/feedbacks/client/" + clientId + "/pyramid/" + nonExistentPyramid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetMyFeedbackForNonExistentPyramid() throws Exception {
        UUID nonExistentPyramid = UUID.randomUUID();
        mockMvc.perform(get("/api/mod/feedbacks/my-pyramid/" + nonExistentPyramid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetFeedbackWhenNoFeedbackExists_ShouldReturnNotFound() throws Exception {
        String newClientToken = loginAndGetToken("kkaktus", "P@ssw0rd!");

        UUID newClientId = UUID.fromString("00000000-0000-0000-0000-000000000012");

        mockMvc.perform(get("/api/mod/feedbacks/client/" + newClientId + "/pyramid/" + foodPyramidId)
                        .header("Authorization", "Bearer " + newClientToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void testClientTriesToAccessOtherClientFeedback_ShouldReturnForbiddenOrNotFound() throws Exception {
        UUID otherClientId = UUID.randomUUID();

        mockMvc.perform(get("/api/mod/feedbacks/client/" + otherClientId + "/pyramid/" + foodPyramidId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    public void testGetFeedbackWithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/mod/feedbacks/my-pyramid/" + foodPyramidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnFoodPyramidDetailsWithFeedbacksAndClients() throws Exception {
        mockMvc.perform(get("/api/mod/food-pyramids/" + existingFoodPyramidId)
                        .header("Authorization", "Bearer " + dieticianToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodPyramid.id").value(existingFoodPyramidId.toString()))
                .andExpect(jsonPath("$.feedbacks").isArray())
                .andExpect(jsonPath("$.clients").isArray());
    }

    @Test
    public void shouldReturnNotFoundForNonExistentFoodPyramid() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/mod/food-pyramids/" + randomId)
                        .header("Authorization", "Bearer " + dieticianToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnForbiddenForNonDieticianUser() throws Exception {
        String clientToken = loginAndGetToken("mmortadella", "P@ssw0rd!");

        mockMvc.perform(get("/api/mod/food-pyramids/" + existingFoodPyramidId)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUnauthorizedIfNoToken() throws Exception {
        mockMvc.perform(get("/api/mod/food-pyramids/" + existingFoodPyramidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}