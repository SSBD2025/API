package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK15Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LockTokenService lockTokenService;

    private final String accountId ="00000000-0000-0000-0000-000000000001";

    private String accessToken;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        String loginRequestJson = """
                {
                  "login": "jcheddar",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk()).andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        token = objectMapper.readTree(responseJson).get("value").asText();
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    void loginUser() throws Exception {
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
        accessToken = objectMapper.readTree(responseJson).get("value").asText();
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        String expectedToken = "fixed-token-123";

        when(lockTokenService.generateToken(any(UUID.class), anyLong()))
                .thenReturn(new SensitiveDTO(expectedToken));

        mockMvc.perform(get("/api/account/" + accountId).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.login").value("jcheddar"))
                .andExpect(jsonPath("$.account.id").value(accountId))
                .andExpect(jsonPath("$.account.email").value("jcheddar@example.com"))
                .andExpect(jsonPath("$.account.active").value(true))
                .andExpect(jsonPath("$.account.firstName").value("Justin"))
                .andExpect(jsonPath("$.account.lastName").value("Cheddar"))
                .andExpect(jsonPath("$.account.verified").value(true))
                .andExpect(jsonPath("$.roles[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$.roles[0].active").value(true))
                .andExpect(jsonPath("$.lockToken").value(expectedToken));
    }

    @Test
    void shouldReturn403WhenNoAuthorization() throws Exception {
        mockMvc.perform(get("/api/account/" + accountId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        UUID randomUUID = UUID.randomUUID();

        mockMvc.perform(get("/api/account/" + randomUUID.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400ForInvalidUuidFormat() throws Exception {
        mockMvc.perform(get("/api/account/" + "bajo-jajo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnMe() throws Exception {
        loginUser();

        String expectedToken = "fixed-token-123";

        when(lockTokenService.generateToken(any(UUID.class), anyLong()))
                .thenReturn(new SensitiveDTO(expectedToken));

        mockMvc.perform(get("/api/account/me").contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.login").value("agorgonzola"))
                .andExpect(jsonPath("$.account.id").value("00000000-0000-0000-0000-000000000005"))
                .andExpect(jsonPath("$.account.email").value("agorgonzola@example.com"))
                .andExpect(jsonPath("$.account.active").value(true))
                .andExpect(jsonPath("$.account.firstName").value("Anthony"))
                .andExpect(jsonPath("$.account.lastName").value("Gorgonzola"))
                .andExpect(jsonPath("$.account.verified").value(true))
                .andExpect(jsonPath("$.roles[0].roleName").value("CLIENT"))
                .andExpect(jsonPath("$.roles[0].active").value(true))
                .andExpect(jsonPath("$.lockToken").value(expectedToken));

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + accessToken)).andReturn();
    }

    @Test
    void shouldReturn403WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/account/me"))
                .andExpect(status().isUnauthorized());
    }
}
