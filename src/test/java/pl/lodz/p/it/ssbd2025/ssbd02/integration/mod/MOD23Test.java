package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientBloodTestReportDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.mappers.ClientBloodTestReportMapper;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Client;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientBloodTestReportNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.ClientNotFoundException;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.ModTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.repository.ClientBloodTestReportRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mod.services.implementations.ClientBloodTestReportService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// DIETICIAN INSERT BLOOD TEST RESULTS //
// DIETICIAN INSERT BLOOD TEST RESULTS //
// DIETICIAN INSERT BLOOD TEST RESULTS //
// PS

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(AsyncTestConfig.class)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD23Test extends BaseIntegrationTest {

    private String adminToken;
    private String dietToken;
    private String clientToken;

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
    public void insertSuccessfully() throws Exception {
        String payload = """
    {
        "results": [
            {
                "result": 44.444,
                "bloodParameter": {
                    "name": "HGB"
                }
            }
        ]
    }
    """;

        mockMvc.perform(post("http://localhost:8080/api/mod/blood-test-reports/client/{clientId}", clientId)
                        .content(payload)
                        .header("Authorization", "Bearer " + dietToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        modTestHelper.setClientContext();
        List<ClientBloodTestReportDTO> reports = clientBloodTestReportService.getAllByClientLogin();
        modTestHelper.resetContext();

        boolean containsResult = reports.stream()
                .flatMap(r -> r.getResults().stream())
                .anyMatch(result -> result.getResult() == 44.444);

        Assertions.assertTrue(containsResult);
    }

    // NEGATIVE TESTS //
    // NEGATIVE TESTS //
    // NEGATIVE TESTS //

    @Test
    public void insertFailure_BadRequest_malformed() throws Exception {
        String payload = """
    {
        "results": [
            {
                "result": 44.444,
                "bloodParameter": {
                    "name": "HGB"
                    "description": "cool description"
                },
            }
        ]
    }
    """;

        mockMvc.perform(post("http://localhost:8080/api/mod/blood-test-reports/client/{clientId}", clientId)
                        .content(payload)
                        .header("Authorization", "Bearer " + dietToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void insertFailure_BadRequest_negative() throws Exception {
        String payload = """
    {
        "results": [
            {
                "result": -1,
                "bloodParameter": {
                    "name": "HGB"
                    "description": "cool description"
                },
            }
        ]
    }
    """;

        mockMvc.perform(post("http://localhost:8080/api/mod/blood-test-reports/client/{clientId}", clientId)
                        .content(payload)
                        .header("Authorization", "Bearer " + dietToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void insertFailure_BadRequest_missing_parameter() throws Exception {
        String payload = """
    {
        "results": [
            {
                "result": 1,
            }
        ]
    }
    """;

        mockMvc.perform(post("http://localhost:8080/api/mod/blood-test-reports/client/{clientId}", clientId)
                        .content(payload)
                        .header("Authorization", "Bearer " + dietToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void insertFailure_BadRequest_nonexistent_parameter() throws Exception {
        String payload = """
    {
        "results": [
            {
                "result": 44.444,
                "bloodParameter": {
                    "name": "NONEXISTENT"
                }
            }
        ]
    }
    """;

        mockMvc.perform(post("http://localhost:8080/api/mod/blood-test-reports/client/{clientId}", clientId)
                        .content(payload)
                        .header("Authorization", "Bearer " + dietToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void insertFailure_Unauthorized() throws Exception {
        String payload = """
    {
        "results": [
            {
                "result": 44.444,
                "bloodParameter": {
                    "name": "HGB"
                    "description": "cool description"
                },
            }
        ]
    }
    """;

        mockMvc.perform(post("http://localhost:8080/api/mod/blood-test-reports/client/{clientId}", clientId)
                        .content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void insertFailure_Forbidden() throws Exception {
        String payload = """
    {
        "results": [
            {
                "result": 44.444,
                "bloodParameter": {
                    "name": "HGB"
                }
            }
        ]
    }
    """;

        mockMvc.perform(post("http://localhost:8080/api/mod/blood-test-reports/client/{clientId}", clientId)
                        .content(payload)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
