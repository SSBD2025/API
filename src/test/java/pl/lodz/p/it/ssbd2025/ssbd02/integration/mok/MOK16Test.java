package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK16Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountTestHelper accountTestHelper;

    @MockitoBean
    private EmailService emailService;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        doNothing().when(emailService).sendActivationMail(any(), any(), any(), any(), any());
        doNothing().when(emailService).sendAdminLoginEmail(anyString(), anyString(), anyString(), any());
        doNothing().when(emailService).sendActivateAccountEmail(any(), any(), any());


        accountTestHelper.activateAndVerifyByLogin("agorgonzola"); // CLIENT
        accountTestHelper.activateAndVerifyByLogin("jcheddar"); // ADMIN
        adminToken = loginAndGetToken("jcheddar", "P@ssw0rd!");
    }

    private String loginAndGetToken(String login, String password) throws Exception {
        doNothing().when(emailService).sendActivationMail(any(), any(), any(), any(), any());
        doNothing().when(emailService).sendAdminLoginEmail(anyString(), anyString(), anyString(), any());
        doNothing().when(emailService).sendActivateAccountEmail(any(), any(), any());
        String loginJson = String.format("""
        {
          "login": "%s",
          "password": "%s"
        }
        """, login, password);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        return new org.json.JSONObject(result.getResponse().getContentAsString()).getString("value");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + adminToken));
    }

    @Test
    public void shouldReturnListOfAccountsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/account")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(empty()))));
    }

    @Test
    public void shouldReturnOnlyActiveAccounts() throws Exception {
        accountTestHelper.activateByLogin("agorgonzola");
        accountTestHelper.getClientByLogin("agorgonzola").setVerified(false);
        mockMvc.perform(get("/api/account")
                        .param("active", "true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].account.active", everyItem(is(true))));
    }

    @Test
    public void shouldReturnOnlyVerifiedAccounts() throws Exception {
        accountTestHelper.verifyByLogin("agorgonzola");
        mockMvc.perform(get("/api/account")
                        .param("verified", "true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].account.verified", everyItem(is(true))));
    }

    @Test
    public void shouldReturnOnlyUnverifiedAccounts() throws Exception {
        accountTestHelper.activateByLogin("agorgonzola");
        mockMvc.perform(get("/api/account")
                        .param("verified", "false")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].account.verified", everyItem(is(false))));
    }

    @Test
    public void shouldReturnOnlyInactiveAccounts() throws Exception {
        var account = accountTestHelper.getClientByLogin("agorgonzola");
        account.setActive(false);
        mockMvc.perform(get("/api/account")
                        .param("active", "false")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].account.active", everyItem(is(false))));
    }

    @Test
    public void shouldReturn403IfNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/account"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturn403IfNotAdmin() throws Exception {
        String clientToken = loginAndGetToken("agorgonzola", "P@ssw0rd!");

        mockMvc.perform(get("/api/account")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken));
    }
}
