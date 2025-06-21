package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.SurveyMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.ActivityLevel;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.NutritionGoal;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FoodPyramidTestHelper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOD24Test extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FoodPyramidTestHelper foodPyramidTestHelper;
    @Autowired private SurveyMapper surveyMapper;

    private String clientToken;
    private final String login = "agorgonzola";
    private final String password = "P@ssw0rd!";

    private static final String ENDPOINT = "/api/mod/clients/permanent-survey";

    private String loginAndGetToken(String login, String password) throws Exception {
        String loginJson = """
            {
              "login": "%s",
              "password": "%s"
            }
            """.formatted(login, password);

        return objectMapper.readTree(
                        mockMvc.perform(post("/api/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginJson))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString())
                .get("value").asText();
    }

    private SurveyDTO fetchCurrentSurvey() throws Exception {
        MvcResult result = mockMvc.perform(get(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), SurveyDTO.class);
    }

    @BeforeEach
    public void setup() throws Exception {
        clientToken = loginAndGetToken(login, password);
    }

    @AfterEach
    public void logout() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken));
    }

    @Test
    public void shouldEditPermanentSurveySuccessfully() throws Exception {
        SurveyDTO existing = fetchCurrentSurvey();

        existing.setHeight(177);
        existing.setMealsPerDay(4);
        existing.setDietPreferences(List.of("wege", "lowcarb"));
        existing.setActivityLevel(ActivityLevel.MODERATE);
        existing.setNutritionGoal(NutritionGoal.REDUCTION);
        existing.setMealTimes(List.of(
                Timestamp.from(Instant.now().plusSeconds(3600)),
                Timestamp.from(Instant.now().plusSeconds(7200)),
                Timestamp.from(Instant.now().plusSeconds(10800)),
                Timestamp.from(Instant.now().plusSeconds(14400))
        ));
        existing.setEatingHabits("Jem bardzo nieregularnie, dużo przekąsek.");
        existing.setAllergies(List.of("gluten"));
        existing.setIllnesses(List.of("Hashimoto"));
        existing.setMedications(List.of("Letrox"));
        existing.setSmokes(true);
        existing.setDrinksAlcohol(false);

        existing.setLockToken(fetchCurrentSurvey().getLockToken());

        String body = objectMapper.writeValueAsString(existing);

        MvcResult putResult = mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        SurveyDTO updated = objectMapper.readValue(putResult.getResponse().getContentAsString(), SurveyDTO.class);

        assertThat(updated.getHeight()).isEqualTo(177);
        assertThat(updated.getMealsPerDay()).isEqualTo(4);
        assertThat(updated.getDietPreferences()).containsExactlyInAnyOrder("wege", "lowcarb");
        assertThat(updated.getActivityLevel()).isEqualTo(ActivityLevel.MODERATE);
        assertThat(updated.getNutritionGoal()).isEqualTo(NutritionGoal.REDUCTION);
        assertThat(updated.getEatingHabits()).contains("nieregularnie");
        assertThat(updated.isSmokes()).isTrue();
        assertThat(updated.isDrinksAlcohol()).isFalse();
    }

    @Test
    public void shouldFailWithInvalidLockToken() throws Exception {
        SurveyDTO existing = fetchCurrentSurvey();
        existing.setLockToken("invalid-token");

        String body = objectMapper.writeValueAsString(existing);

        mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldFailWithTooShortEatingHabits() throws Exception {
        SurveyDTO existing = fetchCurrentSurvey();
        existing.setEatingHabits("a");
        existing.setLockToken(fetchCurrentSurvey().getLockToken());

        String body = objectMapper.writeValueAsString(existing);

        mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailWithTooLowHeight() throws Exception {
        SurveyDTO existing = fetchCurrentSurvey();
        existing.setHeight(0);
        existing.setLockToken(fetchCurrentSurvey().getLockToken());

        String body = objectMapper.writeValueAsString(existing);

        mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailWhenLockTokenIsMissing() throws Exception {
        SurveyDTO existing = fetchCurrentSurvey();
        existing.setLockToken(null);

        String body = objectMapper.writeValueAsString(existing);

        mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailWhenSurveyNotFound() throws Exception {
        String modLogin = "kkaktus";
        String modPassword = "P@ssw0rd!";
        String modToken = loginAndGetToken(modLogin, modPassword);

        Survey fake = new Survey();
        SurveyDTO nonexistent = surveyMapper.toSurveyDTO(fake);
        nonexistent.setClientId(UUID.randomUUID());
        nonexistent.setHeight(180.0);
        nonexistent.setDateOfBirth(Timestamp.from(Instant.now().minusSeconds(60L * 60 * 24 * 365 * 25))); // 25 lat temu
        nonexistent.setGender(true);
        nonexistent.setMealsPerDay(3);
        nonexistent.setDietPreferences(List.of("wege"));
        nonexistent.setAllergies(List.of("gluten"));
        nonexistent.setIllnesses(List.of("Hashimoto"));
        nonexistent.setMedications(List.of("Letrox"));
        nonexistent.setActivityLevel(ActivityLevel.MODERATE);
        nonexistent.setNutritionGoal(NutritionGoal.MAINTENANCE);
        nonexistent.setMealTimes(List.of(
                Timestamp.from(Instant.now().plusSeconds(3600)),
                Timestamp.from(Instant.now().plusSeconds(7200)),
                Timestamp.from(Instant.now().plusSeconds(10800))
        ));
        nonexistent.setEatingHabits("Normalne jedzenie");
        nonexistent.setSmokes(true);
        nonexistent.setDrinksAlcohol(false);
        nonexistent.setLockToken(UUID.randomUUID().toString());

        String body = objectMapper.writeValueAsString(nonexistent);

        mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + modToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + modToken));
    }

    @Test
    public void shouldFailConcurrentUpdateWithConflict() throws Exception {
        SurveyDTO surveySession1 = fetchCurrentSurvey();
        SurveyDTO surveySession2 = fetchCurrentSurvey();

        surveySession1.setHeight(180);
        surveySession1.setLockToken(fetchCurrentSurvey().getLockToken());
        String bodySession1 = objectMapper.writeValueAsString(surveySession1);

        MvcResult result1 = mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodySession1))
                .andExpect(status().isOk())
                .andReturn();

        SurveyDTO updatedSurvey = objectMapper.readValue(result1.getResponse().getContentAsString(), SurveyDTO.class);

        surveySession2.setHeight(190);
        String bodySession2 = objectMapper.writeValueAsString(surveySession2);

        mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodySession2))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldFailWhenMealTimesIsNull() throws Exception {
        SurveyDTO existing = fetchCurrentSurvey();
        existing.setMealTimes(null);
        existing.setLockToken(fetchCurrentSurvey().getLockToken());

        String body = objectMapper.writeValueAsString(existing);

        mockMvc.perform(put(ENDPOINT)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
