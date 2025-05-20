package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import org.json.JSONObject;
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
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UserRoleDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOK14Test extends BaseIntegrationTest { //LOGOUT
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AccountTestHelper accountTestHelper;

    @MockitoBean
    private JavaMailSender mailSender;

    String adminToken;

    @BeforeEach
    void setup() throws Exception {
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
        adminToken = objectMapper.readTree(responseJson).get("value").asText();

        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + adminToken)).andReturn();
    }

    // POSITIVE TESTS //
    // POSITIVE TESTS //
    // POSITIVE TESTS //

    @Test
    public void clientLogoutTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "clientLogoutTest",
                "P@ssw0rd!",
                null,
                null,
                "clientLogoutTest",
                "clientLogoutTest",
                "clientLogoutTest@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null
        );

        String loginRequestJson = """
        {
          "login": "clientLogoutTest",
          "password": "P@ssw0rd!"
        }
        """;

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        accountTestHelper.verifyByLogin("clientLogoutTest"); //only for tests

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();
        Assertions.assertTrue(body.contains("value"));
        JSONObject json = new JSONObject(body);

        String accessToken = json.getString("value");

        mockMvc.perform(get("/api/account/me")
                .header("Authorization", "Bearer "+accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + accessToken)).andReturn();

        mockMvc.perform(get("/api/account/me")
                .header("Authorization", "Bearer "+accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void clientLogoutCookieClearTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "clientLogoutCookieClearTest",
                "P@ssw0rd!",
                null,
                null,
                "clientLogoutCookieClearTest",
                "clientLogoutCookieClearTest",
                "clientLogoutCookieClearTest@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null
        );

        String loginRequestJson = """
        {
          "login": "clientLogoutCookieClearTest",
          "password": "P@ssw0rd!"
        }
        """;

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        accountTestHelper.verifyByLogin("clientLogoutCookieClearTest"); //only for tests


        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(status().isOk()).andReturn();
        Cookie cookie = result.getResponse().getCookie("refreshToken");
        String body = result.getResponse().getContentAsString();
        Assertions.assertTrue(body.contains("value"));
        JSONObject json = new JSONObject(body);

        String accessToken = json.getString("value");

        mockMvc.perform(get("/api/account/me")
                        .header("Authorization", "Bearer "+accessToken))
                .andExpect(status().isOk());

        MvcResult response2 = mockMvc.perform(post("/api/account/refresh")
                        .cookie(cookie))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(status().isOk()).andReturn();

        Cookie cookie2 = response2.getResponse().getCookie("refreshToken");
        JSONObject json2 = new JSONObject(response2.getResponse().getContentAsString());
        String accessToken2 = json2.getString("value");

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + accessToken2)).andReturn();

        mockMvc.perform(get("/api/account/me")
                        .header("Authorization", "Bearer "+accessToken2))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/account/refresh")
                        .cookie(cookie2))
                .andExpect(status().isUnauthorized());
    }

    // NEGATIVE TESTS //
    // NEGATIVE TESTS //
    // NEGATIVE TESTS //

    @Test
    public void clientLogoutInvalidTokenTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "clientLogoutInvalidTokenTest",
                "P@ssw0rd!",
                null,
                null,
                "clientLogoutInvalidTokenTest",
                "clientLogoutInvalidTokenTest",
                "clientLogoutInvalidTokenTest@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null
        );

        String loginRequestJson = """
        {
          "login": "clientLogoutInvalidTokenTest",
          "password": "P@ssw0rd!"
        }
        """;

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        accountTestHelper.verifyByLogin("clientLogoutInvalidTokenTest"); //only for tests

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();
        Assertions.assertTrue(body.contains("value"));
        JSONObject json = new JSONObject(body);

        String accessToken = json.getString("value");

        mockMvc.perform(get("/api/account/me")
                .header("Authorization", "Bearer "+accessToken))
                .andExpect(status().isOk());

        accessToken = accessToken + "a";

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }
}
