package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOK16 extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JavaMailSender mailSender;

    private String adminToken;
    private String clientToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAndGetToken("jcheddar", "P@ssw0rd!");
        clientToken = loginAndGetToken("agorgonzola", "P@ssw0rd!");
    }

    private String loginAndGetToken(String login, String password) throws Exception {
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

        return new org.json.JSONObject(result.getResponse().getContentAsString()).getString("accessToken");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + adminToken));
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken));
    }


    @Test
    public void shouldReturnListOfAccountsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/account")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnFilteredAccountsByActiveStatus() throws Exception {
        mockMvc.perform(get("/api/account")
                        .param("active", "true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void shouldReturnFilteredAccountsByVerifiedStatus() throws Exception {
        mockMvc.perform(get("/api/account")
                        .param("verified", "true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }


    @Test
    public void shouldReturn403IfNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/account"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturn403IfNotAdmin() throws Exception {
        mockMvc.perform(get("/api/account")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }
}
