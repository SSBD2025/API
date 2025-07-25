package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.AsyncTestConfig;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.PeriodicSurveyDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.PeriodicSurvey;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.ModTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.PeriodicSurveyHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.PeriodicSurveyRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientBloodTestReportService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// CLIENT EDITS LATEST PERIODIC SURVEY //
// CLIENT EDITS LATEST PERIODIC SURVEY //
// CLIENT EDITS LATEST PERIODIC SURVEY //
// PS

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(AsyncTestConfig.class)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD29Test extends BaseIntegrationTest {
    private String adminToken;
    private String dietToken;
    private String clientToken;

    @Autowired
    private PeriodicSurveyHelper helper;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    private final UUID clientId = UUID.fromString("00000000-0000-0000-0000-000000000006");

    @Autowired
    private ClientBloodTestReportService clientBloodTestReportService;
    @Autowired
    private ModTestHelper modTestHelper;
    @Autowired
    private PeriodicSurveyRepository periodicSurveyRepository;

    @BeforeEach
    void setup() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        String adminLoginPayload = """
            {"login": "jcheddar","password": "P@ssw0rd!"}
        """;
        MvcResult adminLoginResult = mockMvc.perform(post("/api/account/login").contentType(MediaType.APPLICATION_JSON).content(adminLoginPayload)).andExpect(status().isOk()).andReturn();
        String adminResponseJson = adminLoginResult.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(adminResponseJson).get("value").asText();

        String dietLoginPayload = """
            {"login": "tcheese","password": "P@ssw0rd!"}
        """;
        MvcResult dietLoginResult = mockMvc.perform(post("/api/account/login").contentType(MediaType.APPLICATION_JSON).content(dietLoginPayload)).andExpect(status().isOk()).andReturn();
        String dietResponseJson = dietLoginResult.getResponse().getContentAsString();
        dietToken = objectMapper.readTree(dietResponseJson).get("value").asText();

        String clientLoginPayload = """
            {"login": "agorgonzola","password": "P@ssw0rd!"}
        """;
        MvcResult loginResult = mockMvc.perform(post("/api/account/login").contentType(MediaType.APPLICATION_JSON).content(clientLoginPayload)).andExpect(status().isOk()).andReturn();
        String responseJson = loginResult.getResponse().getContentAsString();
        clientToken = objectMapper.readTree(responseJson).get("value").asText();
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + adminToken)).andReturn();
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken)).andReturn();
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + dietToken)).andReturn();
    }

    // POSITIVE TESTS //
    // POSITIVE TESTS //
    // POSITIVE TESTS //

    @Test
    public void clientSuccessfullyEdits() throws Exception {
        PeriodicSurveyDTO periodicSurveyDTO = new PeriodicSurveyDTO(
                null,
                null,
                null,
                80.0,
                "120/80",
                100.0,
                null,
                null,
                null
        );

        String body = objectMapper.writeValueAsString(periodicSurveyDTO);

        MvcResult res = mockMvc.perform(post("/api/mod/clients/periodic-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value("00000000-0000-0000-0000-000000000006"))
                .andExpect(jsonPath("$.weight").value(80.0))
                .andExpect(jsonPath("$.bloodPressure").value("120/80"))
                .andExpect(jsonPath("$.bloodSugarLevel").value(100.0))
                .andReturn();

        String id = JsonPath.read(res.getResponse().getContentAsString(), "$.id");
        UUID uuid = UUID.fromString(id);
        PeriodicSurvey periodicSurveyFromDB = helper.getSurveyById(uuid);

        Assertions.assertNotNull(periodicSurveyFromDB);
        Assertions.assertEquals("00000000-0000-0000-0000-000000000006", periodicSurveyFromDB.getClient().getId().toString());
        Assertions.assertEquals(80.0, periodicSurveyFromDB.getWeight());
        Assertions.assertEquals("120/80", periodicSurveyFromDB.getBloodPressure());
        Assertions.assertEquals(100.0, periodicSurveyFromDB.getBloodSugarLevel());

        MvcResult result = mockMvc.perform(get("/api/mod/clients/periodic-survey/latest")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(80.0))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        Map<String, Object> jsonMap = objectMapper.readValue(content, new TypeReference<>() {});
        jsonMap.put("weight", 99.9);
        jsonMap.put("id", null);
        jsonMap.put("measurementDate", null);
        jsonMap.put("createdAt", null);
        jsonMap.put("clientId", null);

        String updatedPayload = objectMapper.writeValueAsString(jsonMap);

        mockMvc.perform(put("/api/mod/clients/periodic-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(99.9));
    }

    // NEGATIVE TESTS //
    // NEGATIVE TESTS //
    // NEGATIVE TESTS //

    @Test
    public void editFailure_InvalidLockToken() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mod/clients/periodic-survey/latest")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(87.7))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        Map<String, Object> jsonMap = objectMapper.readValue(content, new TypeReference<>() {});
        jsonMap.put("weight", 99.9);
        jsonMap.put("id", null);
        jsonMap.put("measurementDate", null);
        jsonMap.put("createdAt", null);
        jsonMap.put("clientId", null);
        jsonMap.put("lockToken", "abc");

        String updatedPayload = objectMapper.writeValueAsString(jsonMap);

        mockMvc.perform(put("/api/mod/clients/periodic-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void editFailure_BadRequest() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mod/clients/periodic-survey/latest")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(87.7))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        Map<String, Object> jsonMap = objectMapper.readValue(content, new TypeReference<>() {});
        jsonMap.put("weight", 99.9);

        String updatedPayload = objectMapper.writeValueAsString(jsonMap);

        mockMvc.perform(put("/api/mod/clients/periodic-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void editFailure_BadRequest_tooHeavy() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mod/clients/periodic-survey/latest")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(87.7))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        Map<String, Object> jsonMap = objectMapper.readValue(content, new TypeReference<>() {});
        jsonMap.put("weight", 999.9);

        String updatedPayload = objectMapper.writeValueAsString(jsonMap);

        mockMvc.perform(put("/api/mod/clients/periodic-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void editFailure_BadRequest_tooLight() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mod/clients/periodic-survey/latest")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(87.7))
                .andReturn();

        String content = result.getResponse().getContentAsString();

        Map<String, Object> jsonMap = objectMapper.readValue(content, new TypeReference<>() {});
        jsonMap.put("weight", 1);

        String updatedPayload = objectMapper.writeValueAsString(jsonMap);

        mockMvc.perform(put("/api/mod/clients/periodic-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isBadRequest());
    }
}