package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.AsyncTestConfig;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(AsyncTestConfig.class)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD14Test extends BaseIntegrationTest {
    @MockitoBean
    private JavaMailSender mailSender;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private String token;

    @BeforeEach
    void setup() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

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
        token = objectMapper.readTree(responseJson).get("value").asText();
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void getClientsWithSearchPhraseEmailTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/get-clients-by-dietician")
                        .header("Authorization", "Bearer " + token)
                        .param("searchPhrase", "agorgonzola@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getClientsWithSearchPhraseFirstNameTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/get-clients-by-dietician")
                        .header("Authorization", "Bearer " + token)
                        .param("searchPhrase", "Anthony")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getClientsWithSearchPhraseLastNameTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/get-clients-by-dietician")
                        .header("Authorization", "Bearer " + token)
                        .param("searchPhrase", "Anthony")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getClientsWithSearchPhraseEmailFragmentTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/get-clients-by-dietician")
                        .header("Authorization", "Bearer " + token)
                        .param("searchPhrase", "ago")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getClientsWithSearchPhraseFirstNameFragmentTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/get-clients-by-dietician")
                        .header("Authorization", "Bearer " + token)
                        .param("searchPhrase", "antho")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getClientsWithSearchPhraseLastNameFragmentTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/get-clients-by-dietician")
                        .header("Authorization", "Bearer " + token)
                        .param("searchPhrase", "gorg")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getClientsWithWrongSearchPhraseTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/get-clients-by-dietician")
                        .header("Authorization", "Bearer " + token)
                        .param("searchPhrase", "sajhfgashjfgasj")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void getClientsWithSearchPhraseUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/get-clients-by-dietician")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("searchPhrase", "ago"))
                .andExpect(status().isUnauthorized());
    }
}
