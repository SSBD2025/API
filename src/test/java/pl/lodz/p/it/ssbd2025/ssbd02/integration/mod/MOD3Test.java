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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Survey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//Przeglądanie szczegółów konkretnego klienta
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD3Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JavaMailSender mailSender;

    private String clientId = "00000000-0000-0000-0000-000000000006";

    @BeforeEach
    void setup() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void getClientDetailsTest() throws Exception {
        String loginRequestJson = """
        {
          "login": "tcheese",
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

        mockMvc.perform(get("/api/mod/dieticians/" + clientId + "/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Anthony"))
                .andExpect(jsonPath("$.lastName").value("Gorgonzola"))
                .andExpect(jsonPath("$.survey.height").value(180.0))
                .andExpect(jsonPath("$.survey.dietPreferences[0]").value("vegetarian"))
                .andExpect(jsonPath("$.survey.allergies[1]").value("lactose"))
                .andExpect(jsonPath("$.periodicSurvey.length()").value(7))
                .andExpect(jsonPath("$.periodicSurvey[0].weight").value(80.0))
                .andExpect(jsonPath("$.bloodTestReport[0].results[0].result").value(13.7))
                .andExpect(jsonPath("$.bloodTestReport[1].results[2].bloodParameter.description").value("Platelets"));

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void getClientDetailsClientNotFoundTest() throws Exception {
        String loginRequestJson = """
        {
          "login": "tcheese",
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

        MvcResult result = mockMvc.perform(get("/api/mod/dieticians/" + "00078900-0000-0880-0000-009010000006" + "/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andReturn();

        Assertions.assertEquals("client_not_found", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void getClientDetailsInvalidIdTest() throws Exception {
        String loginRequestJson = """
        {
          "login": "tcheese",
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

        mockMvc.perform(get("/api/mod/dieticians/" + "123" + "/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.violations").isArray())
                        .andExpect(jsonPath("$.violations.length()").value(1))
                        .andExpect(jsonPath("$.violations[0].fieldName").value("clientId"))
                        .andExpect(jsonPath("$.violations[0].message").value("Invalid value '123' for parameter 'clientId'. Expected 'UUID' type."));


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void getClientDetailsNoTokenProvidedTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/" + clientId + "/details")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getClientDetailsInvalidTokenTest() throws Exception {
        String loginRequestJson = """
        {
          "login": "tcheese",
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

        mockMvc.perform(get("/api/mod/dieticians/" + clientId + "/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token + "1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void getClientDetailsAsClientTest() throws Exception {
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

        mockMvc.perform(get("/api/mod/dieticians/" + clientId + "/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token + "1"))
                .andExpect(status().isUnauthorized()); // ???

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void getClientDetailsAsAdminTest() throws Exception {
        String loginRequestJson = """
        {
          "login": "jcheddar",
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

        mockMvc.perform(get("/api/mod/dieticians/" + clientId + "/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token + "1"))
                .andExpect(status().isUnauthorized()); // ???

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }
}
