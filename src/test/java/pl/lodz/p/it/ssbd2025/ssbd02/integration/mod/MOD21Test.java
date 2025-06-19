package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.PeriodicSurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.PeriodicSurveyHelper;

import java.util.UUID;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//Wypełnienie ankiety okresowej
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD21Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PeriodicSurveyHelper helper;

    @MockitoBean
    private JavaMailSender mailSender;

    private String clientToken;

    PeriodicSurveyDTO periodicSurveyDTO = new PeriodicSurveyDTO(
            null,
            null,
            null,
            80.0,
            "120/80",
            100.0,
            null,
            null
    );



    @BeforeEach
    void setup() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

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
        clientToken = objectMapper.readTree(responseJson).get("value").asText();

    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken)).andReturn();
    }


    @Test
    public void submitPeriodicSurveyPositiveTest() throws Exception {

        String body = objectMapper.writeValueAsString(periodicSurveyDTO);

        MvcResult result = mockMvc.perform(post("/api/mod/clients/periodic-survey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", "Bearer " + clientToken))
        .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value("00000000-0000-0000-0000-000000000006"))
                .andExpect(jsonPath("$.weight").value(80.0))
                .andExpect(jsonPath("$.bloodPressure").value("120/80"))
                .andExpect(jsonPath("$.bloodSugarLevel").value(100.0))
                .andReturn();

        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        UUID uuid = UUID.fromString(id);
        PeriodicSurvey periodicSurveyFromDB = helper.getSurveyById(uuid);

        Assertions.assertNotNull(periodicSurveyFromDB);
        Assertions.assertEquals("00000000-0000-0000-0000-000000000006", periodicSurveyFromDB.getClient().getId().toString());
        Assertions.assertEquals(80.0, periodicSurveyFromDB.getWeight());
        Assertions.assertEquals("120/80", periodicSurveyFromDB.getBloodPressure());
        Assertions.assertEquals(100.0, periodicSurveyFromDB.getBloodSugarLevel());

    }

    @Test
    public void submitPeriodicSurveyBefore24hTest() throws Exception {
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
        String body = objectMapper.writeValueAsString(periodicSurveyDTO);

        MvcResult result = mockMvc.perform(post("/api/mod/clients/periodic-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weight").value(80.0))
                .andExpect(jsonPath("$.bloodPressure").value("120/80"))
                .andExpect(jsonPath("$.bloodSugarLevel").value(100.0))
                .andReturn();

        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        UUID uuid = UUID.fromString(id);
        PeriodicSurvey periodicSurveyFromDB = helper.getSurveyById(uuid);

        Assertions.assertNotNull(periodicSurveyFromDB);
        Assertions.assertEquals(80.0, periodicSurveyFromDB.getWeight());
        Assertions.assertEquals("120/80", periodicSurveyFromDB.getBloodPressure());
        Assertions.assertEquals(100.0, periodicSurveyFromDB.getBloodSugarLevel());

        MvcResult result2 = mockMvc.perform(post("/api/mod/clients/periodic-survey")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andReturn();
        Assertions.assertEquals("periodic_survey_too_soon", result2.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void submitPeriodicSurveyNoTokenProvidedTest() throws Exception {
        mockMvc.perform(post("/api/mod/clients/periodic-survey"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    public void submitPeriodicSurveyInvalidTokenProvidedTest() throws Exception {
        mockMvc.perform(post("/api/mod/clients/periodic-survey")
                .header("Authorization", "Bearer " + clientToken + "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void submitPeriodicSurveyAsDieticianTest() throws Exception {
        String loginDieticianRequestJson = """
        {
          "login": "tcheese",
          "password": "P@ssw0rd!"
        }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDieticianRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String dToken = objectMapper.readTree(responseJson).get("value").asText();
        String body = objectMapper.writeValueAsString(periodicSurveyDTO);

        mockMvc.perform(post("/api/mod/clients/periodic-survey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", "Bearer " + dToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + dToken)).andReturn();
    }

    @Test
    public void submitPeriodicSurveyAsAdminTest() throws Exception {
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
        String aToken = objectMapper.readTree(responseJson).get("value").asText();
        String body = objectMapper.writeValueAsString(periodicSurveyDTO);

        mockMvc.perform(post("/api/mod/clients/periodic-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + aToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + aToken)).andReturn();
    }

}
