package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.SurveyHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


//Przeglądanie szczegółów ankiety parametrów stałych
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD10Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SurveyHelper surveyHelper;

    @Autowired
    private LockTokenService lockTokenService;

    @MockitoBean
    private JavaMailSender mailSender;

    UUID clientId = UUID.fromString("00000000-0000-0000-0000-000000000006");

    String loginClientRequestJson = """
        {
          "login": "agorgonzola",
          "password": "P@ssw0rd!"
        }
        """;

    String loginDieticianRequestJson = """
        {
          "login": "tcheese",
          "password": "P@ssw0rd!"
        }
        """;


    @BeforeEach
    void setup() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    //Client tests

    @Test
    public void getSelfPermanentSurveyTest() throws Exception {

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginClientRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String clientToken = objectMapper.readTree(responseJson).get("value").asText();

        MvcResult result = mockMvc.perform(get("/api/mod/clients/permanent-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("00000000-0000-0000-0000-000000000006"))
                .andExpect(jsonPath("$.height").value(180.0))
                .andExpect(jsonPath("$.gender").value(true))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-05-11T22:00:00.000+00:00"))
                .andExpect(jsonPath("$.dietPreferences.length()").value(2))
                .andExpect(jsonPath("$.dietPreferences[0]").value("vegetarian"))
                .andExpect(jsonPath("$.dietPreferences[1]").value("no pork"))
                .andExpect(jsonPath("$.allergies.length()").value(2))
                .andExpect(jsonPath("$.allergies[0]").value("nuts"))
                .andExpect(jsonPath("$.allergies[1]").value("lactose"))
                .andExpect(jsonPath("$.activityLevel").value("MODERATE"))
                .andExpect(jsonPath("$.smokes").value(false))
                .andExpect(jsonPath("$.drinksAlcohol").value(true))
                .andExpect(jsonPath("$.illnesses.length()").value(1))
                .andExpect(jsonPath("$.illnesses[0]").value("hypertension"))
                .andExpect(jsonPath("$.medications.length()").value(1))
                .andExpect(jsonPath("$.medications[0]").value("aspirin"))
                .andExpect(jsonPath("$.mealsPerDay").value(4))
                .andExpect(jsonPath("$.nutritionGoal").value("REDUCTION"))
                .andExpect(jsonPath("$.mealTimes.length()").value(4))
                .andExpect(jsonPath("$.mealTimes[0]").value("2024-01-01T07:00:00.000+00:00"))
                .andExpect(jsonPath("$.mealTimes[1]").value("2024-01-01T11:00:00.000+00:00"))
                .andExpect(jsonPath("$.mealTimes[2]").value("2024-01-01T15:00:00.000+00:00"))
                .andExpect(jsonPath("$.mealTimes[3]").value("2024-01-01T19:00:00.000+00:00"))
                .andExpect(jsonPath("$.eatingHabits").value("I usually eat late at night, and sometimes skip breakfast."))
                .andReturn();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        Survey surveyFromDB = surveyHelper.getSurveyByClientId(clientId);

        Assertions.assertNotNull(surveyFromDB);
        Assertions.assertEquals(180.0, surveyFromDB.getHeight());
        Assertions.assertTrue(surveyFromDB.isGender());
        Assertions.assertEquals(List.of("vegetarian", "no pork"), surveyFromDB.getDietPreferences());
        Assertions.assertEquals(List.of("nuts", "lactose"), surveyFromDB.getAllergies());
        Assertions.assertEquals("MODERATE", surveyFromDB.getActivityLevel().name());
        Assertions.assertFalse(surveyFromDB.isSmokes());
        Assertions.assertTrue(surveyFromDB.isDrinksAlcohol());
        Assertions.assertEquals(List.of("hypertension"), surveyFromDB.getIllnesses());
        Assertions.assertEquals(List.of("aspirin"), surveyFromDB.getMedications());
        Assertions.assertEquals(4, surveyFromDB.getMealsPerDay());
        Assertions.assertEquals("REDUCTION", surveyFromDB.getNutritionGoal().name());
        Assertions.assertEquals(4, surveyFromDB.getMealTimes().size());
        Assertions.assertEquals("I usually eat late at night, and sometimes skip breakfast.", surveyFromDB.getEatingHabits());

        String response = result.getResponse().getContentAsString();
        String lockToken = objectMapper.readTree(response).get("lockToken").asText();

        Assertions.assertDoesNotThrow(() -> lockTokenService.verifyToken(lockToken));

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken)).andReturn();

        SecurityContextHolder.clearContext();
    }


    @Test
    public void getSelfPermanentSurveyNoTokenProvidedTest() throws Exception {
        mockMvc.perform(post("/api/mod/clients/periodic-survey"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getSelfPermanentSurveyInvalidTokenProvidedTest() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginClientRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String clientToken = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(post("/api/mod/clients/periodic-survey")
                        .header("Authorization", "Bearer " + clientToken + "1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken)).andReturn();
    }

    @Test
    public void getSelfPermanentSurveyAsDieticianTest() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDieticianRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String dieticianToken = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(post("/api/mod/clients/periodic-survey")
                        .header("Authorization", "Bearer " + dieticianToken + "1"))
                .andExpect(status().isUnauthorized()); //???

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + dieticianToken)).andReturn();
    }

    @Test
    public void getSelfPermanentSurveyAsAdminTest() throws Exception {
        String loginAdminRequestJson = """
            {
            "login": "jcheddar",
            "password": "P@ssw0rd!"
            }
         """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginAdminRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String adminToken = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(post("/api/mod/clients/periodic-survey")
                        .header("Authorization", "Bearer " + adminToken + "1"))
                .andExpect(status().isUnauthorized()); //???

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + adminToken)).andReturn();
    }

    //Dietician Tests

    @Test
    public void getClientPermanentSurveyTest() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDieticianRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String dieticianToken = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(get("/api/mod/dieticians/" + clientId.toString() + "/permanent-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + dieticianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("00000000-0000-0000-0000-000000000006"))
                .andExpect(jsonPath("$.height").value(180.0))
                .andExpect(jsonPath("$.gender").value(true))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-05-11T22:00:00.000+00:00"))
                .andExpect(jsonPath("$.dietPreferences.length()").value(2))
                .andExpect(jsonPath("$.dietPreferences[0]").value("vegetarian"))
                .andExpect(jsonPath("$.dietPreferences[1]").value("no pork"))
                .andExpect(jsonPath("$.allergies.length()").value(2))
                .andExpect(jsonPath("$.allergies[0]").value("nuts"))
                .andExpect(jsonPath("$.allergies[1]").value("lactose"))
                .andExpect(jsonPath("$.activityLevel").value("MODERATE"))
                .andExpect(jsonPath("$.smokes").value(false))
                .andExpect(jsonPath("$.drinksAlcohol").value(true))
                .andExpect(jsonPath("$.illnesses.length()").value(1))
                .andExpect(jsonPath("$.illnesses[0]").value("hypertension"))
                .andExpect(jsonPath("$.medications.length()").value(1))
                .andExpect(jsonPath("$.medications[0]").value("aspirin"))
                .andExpect(jsonPath("$.mealsPerDay").value(4))
                .andExpect(jsonPath("$.nutritionGoal").value("REDUCTION"))
                .andExpect(jsonPath("$.mealTimes.length()").value(4))
                .andExpect(jsonPath("$.mealTimes[0]").value("2024-01-01T07:00:00.000+00:00"))
                .andExpect(jsonPath("$.mealTimes[1]").value("2024-01-01T11:00:00.000+00:00"))
                .andExpect(jsonPath("$.mealTimes[2]").value("2024-01-01T15:00:00.000+00:00"))
                .andExpect(jsonPath("$.mealTimes[3]").value("2024-01-01T19:00:00.000+00:00"))
                .andExpect(jsonPath("$.eatingHabits").value("I usually eat late at night, and sometimes skip breakfast."));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("tcheese", null, List.of(new SimpleGrantedAuthority("ROLE_DIETICIAN")))
        );

        Survey surveyFromDB = surveyHelper.getSurveyByClientId(clientId);



        Assertions.assertNotNull(surveyFromDB);
        Assertions.assertEquals(180.0, surveyFromDB.getHeight());
        Assertions.assertTrue(surveyFromDB.isGender());
        Assertions.assertEquals(List.of("vegetarian", "no pork"), surveyFromDB.getDietPreferences());
        Assertions.assertEquals(List.of("nuts", "lactose"), surveyFromDB.getAllergies());
        Assertions.assertEquals("MODERATE", surveyFromDB.getActivityLevel().name());
        Assertions.assertFalse(surveyFromDB.isSmokes());
        Assertions.assertTrue(surveyFromDB.isDrinksAlcohol());
        Assertions.assertEquals(List.of("hypertension"), surveyFromDB.getIllnesses());
        Assertions.assertEquals(List.of("aspirin"), surveyFromDB.getMedications());
        Assertions.assertEquals(4, surveyFromDB.getMealsPerDay());
        Assertions.assertEquals("REDUCTION", surveyFromDB.getNutritionGoal().name());
        Assertions.assertEquals(4, surveyFromDB.getMealTimes().size());
        Assertions.assertEquals("I usually eat late at night, and sometimes skip breakfast.", surveyFromDB.getEatingHabits());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + dieticianToken)).andReturn();

        SecurityContextHolder.clearContext();
    }

    @Test
    public void getClientPermanentSurveyNoTokenProvidedTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/" + clientId.toString() + "/permanent-survey"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getClientPermanentSurveyInvalidTokenProvidedTest() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDieticianRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String dieticianToken = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(get("/api/mod/dieticians/" + clientId.toString() + "/permanent-survey")
                        .header("Authorization", "Bearer " + dieticianToken + "1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + dieticianToken)).andReturn();
    }

    @Test
    public void getClientPermanentSurveyAsClientTest() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginClientRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String clientToken = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(get("/api/mod/dieticians/" + clientId.toString() + "/permanent-survey")
                        .header("Authorization", "Bearer " + clientToken + "1"))
                .andExpect(status().isUnauthorized()); //???

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken)).andReturn();
    }

    @Test
    public void getClientPermanentSurveyAsAdminTest() throws Exception {
        String loginAdminRequestJson = """
            {
            "login": "jcheddar",
            "password": "P@ssw0rd!"
            }
         """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginAdminRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String adminToken = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(get("/api/mod/dieticians/" + clientId.toString() + "/permanent-survey")
                        .header("Authorization", "Bearer " + adminToken + "1"))
                .andExpect(status().isUnauthorized()); //???

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + adminToken)).andReturn();
    }

    @Test
    public void getClientPermanentSurveyClientNotFound() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDieticianRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String dieticianToken = objectMapper.readTree(responseJson).get("value").asText();

        MvcResult result = mockMvc.perform(get("/api/mod/dieticians/01234000-0000-0000-0000-000000000006/permanent-survey")
                        .header("Authorization", "Bearer " + dieticianToken))
                        .andExpect(status().isNotFound())
                        .andReturn();

        Assertions.assertEquals("permanent_survey_not_found", result.getResponse().getErrorMessage());


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + dieticianToken)).andReturn();
    }

    @Test
    public void getClientPermanentSurveySurveyNotFound() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDieticianRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String dieticianToken = objectMapper.readTree(responseJson).get("value").asText();

        MvcResult result = mockMvc.perform(get("/api/mod/dieticians/00000000-0000-0000-0000-000000000015/permanent-survey")
                        .header("Authorization", "Bearer " + dieticianToken))
                .andExpect(status().isNotFound())
                .andReturn();

        Assertions.assertEquals("permanent_survey_not_found", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + dieticianToken)).andReturn();
    }

}
