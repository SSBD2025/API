package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.AsyncTestConfig;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.ActivityLevel;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.NutritionGoal;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.ModTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientModService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(AsyncTestConfig.class)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD20Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    ClientModService clientModService;

    @Test
    public void submitPermanentSurveyTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kkaktus",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                180.0,
                dateOfBirth,
                true,
                List.of("wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("kkaktus", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))));
        Survey permanentSurvey = clientModService.getPermanentSurvey();
        SecurityContextHolder.setContext(securityContext);
        Assertions.assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000013"), permanentSurvey.getClient().getId());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNotAssignedDieticianTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "jorzel",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                180.0,
                dateOfBirth,
                true,
                List.of("wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        MvcResult result = mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("no_assigned_dietican", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyPermanentSurveyAlreadyExistTest() throws Exception {
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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                180.0,
                dateOfBirth,
                true,
                List.of("wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        MvcResult result = mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("permanent_survey_already_exists", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNegativeHeightTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                -100.0,
                dateOfBirth,
                true,
                List.of("wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyHeightEqualsZeroTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                0,
                dateOfBirth,
                true,
                List.of("wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyHeightIsGreaterThen220Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                221,
                dateOfBirth,
                true,
                List.of("wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNullDateOfBirthTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                null,
                true,
                List.of("wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNullGenderTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNullDietPreferencesTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                true,
                null,
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyDietPreferencesGreaterThen5Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege", "wege", "wege", "wege", "wege", "wege"),
                List.of(),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNullAllergiesTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                null,
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyAllergiesGreaterThen5Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy", "orzechy", "orzechy", "orzechy", "orzechy", "orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNullActivityLevelTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                null,
                true,
                true,
                List.of(),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNullIllnessesTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                null,
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyIllnessesGreaterThen5Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba", "choroba", "choroba", "choroba", "choroba", "choroba"),
                List.of(),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNullMedicationsTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                null,
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyMedicationsGreaterThen5Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek", "lek", "lek", "lek", "lek", "lek"),
                2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNegativeMealsPerDayTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                -2,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyMealsPerDayEqualsZeroTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                0,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyMealsPerDayGreaterThen10Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                11,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNutritionGoalNullTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                null,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyMealTimesLowerThen1Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                NutritionGoal.MAINTENANCE,
                List.of(),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyMealTimesGreaterThen10Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                NutritionGoal.MAINTENANCE,
                List.of(
                        Timestamp.valueOf("2025-06-15 08:00:00"),
                        Timestamp.valueOf("2025-06-15 09:00:00"),
                        Timestamp.valueOf("2025-06-15 10:00:00"),
                        Timestamp.valueOf("2025-06-15 11:00:00"),
                        Timestamp.valueOf("2025-06-15 12:00:00"),
                        Timestamp.valueOf("2025-06-15 13:00:00"),
                        Timestamp.valueOf("2025-06-15 14:00:00"),
                        Timestamp.valueOf("2025-06-15 15:00:00"),
                        Timestamp.valueOf("2025-06-15 16:00:00"),
                        Timestamp.valueOf("2025-06-15 17:00:00"),
                        Timestamp.valueOf("2025-06-15 18:00:00")),
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyNullMealTimesTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                NutritionGoal.MAINTENANCE,
                null,
                "Lubie sobie dobrze zjesc"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyEatingHabitsNullTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                null
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyEatingHabitsBlankTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                ""
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyEatingHabitsLowerThen10Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "123456789"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyEatingHabitsGreaterThen500Test() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kzachod",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                """
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        """
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .header("Authorization", "Bearer " + token)
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPermanentSurveyUnauthorizedTest() throws Exception {
        LocalDate birthDate = LocalDate.of(1995, 6, 15);
        Timestamp dateOfBirth = Timestamp.valueOf(birthDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime());

        SurveyDTO surveyDTO = new SurveyDTO(
                null,
                null,
                100.0,
                dateOfBirth,
                null,
                List.of("wege"),
                List.of("orzechy"),
                ActivityLevel.LIGHT,
                true,
                true,
                List.of("choroba"),
                List.of("lek"),
                10,
                NutritionGoal.MAINTENANCE,
                List.of(Timestamp.valueOf("2025-06-15 08:00:00"), Timestamp.valueOf("2025-06-15 12:00:00")),
                "tu jest ok tekst"
        );

        String survey = objectMapper.writeValueAsString(surveyDTO);

        mockMvc.perform(post("/api/mod/clients/permanent-survey")
                        .content(survey)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
