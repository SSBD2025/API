package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.I18n;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper.extractTextFromMimeMessage;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOK1 extends BaseIntegrationTest { //LOGIN
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
          "login": "adminlogin",
          "password": "password"
        }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(responseJson).get("accessToken").asText();

        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
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
    public void clientLoginTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "clientLoginTest",
                "clientLoginTest",
                null,
                null,
                "clientLoginTest",
                "clientLoginTest",
                "clientLoginTest@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null
        );

        String loginRequestJson = """
        {
          "login": "clientLoginTest",
          "password": "clientLoginTest"
        }
        """;

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        accountTestHelper.verifyByLogin("clientLoginTest"); //only for tests

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("refreshToken")))
                .andExpect(result -> {
                    Assertions.assertTrue(result.getResponse().getContentAsString().contains("accessToken"));
                    JSONObject json = new JSONObject(result.getResponse().getContentAsString());
                    Assertions.assertTrue(tokenProvider.getRoles(json.getString("accessToken")).contains("CLIENT"));
                });
    }

    @Test
    public void dieticianLoginTest() throws Exception {
        UserRoleDTO.DieticianDTO dieticianDTO = new UserRoleDTO.DieticianDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "dieticianLoginTest",
                "dieticianLoginTest",
                null,
                null,
                "dieticianLoginTest",
                "dieticianLoginTest",
                "dieticianLoginTest@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null
        );

        String loginRequestJson = """
        {
          "login": "dieticianLoginTest",
          "password": "dieticianLoginTest"
        }
        """;

        DieticianDTO dieticianDTO2 = new DieticianDTO(dieticianDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(dieticianDTO2);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        accountTestHelper.activateAndVerifyByLogin("dieticianLoginTest"); //only for tests

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("refreshToken")))
                .andExpect(result -> {
                    Assertions.assertTrue(result.getResponse().getContentAsString().contains("accessToken"));
                    JSONObject json = new JSONObject(result.getResponse().getContentAsString());
                    Assertions.assertTrue(tokenProvider.getRoles(json.getString("accessToken")).contains("DIETICIAN"));
                });
    }

    @Test
    public void adminLoginTest() throws Exception {
        UserRoleDTO.AdminDTO adminDTO = new UserRoleDTO.AdminDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "adminLoginTest",
                "adminLoginTest",
                null,
                null,
                "adminLoginTest",
                "adminLoginTest",
                "adminLoginTest@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null
        );

        String loginRequestJson = """
        {
          "login": "adminLoginTest",
          "password": "adminLoginTest"
        }
        """;

        AdminDTO adminDTO2 = new AdminDTO(adminDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(adminDTO2);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer "+adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        accountTestHelper.activateAndVerifyByLogin("adminLoginTest"); //only for tests

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("refreshToken")))
                .andExpect(result -> {
                    Assertions.assertTrue(result.getResponse().getContentAsString().contains("accessToken"));
                    JSONObject json = new JSONObject(result.getResponse().getContentAsString());
                    Assertions.assertTrue(tokenProvider.getRoles(json.getString("accessToken")).contains("ADMIN"));
                });

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, atLeastOnce()).send(messageCaptor.capture());


        List<MimeMessage> sentMessages = messageCaptor.getAllValues();
        Assertions.assertFalse(sentMessages.isEmpty(), "No emails were sent");

        boolean ipFound = false;
        for (MimeMessage msg : sentMessages) {
            String body = extractTextFromMimeMessage(msg);
            System.out.println(body);
            if (body.contains(I18n.getMessage("email.login_as_admin.body", Language.pl_PL)) || body.contains(I18n.getMessage("email.login_as_admin.body", Language.en_EN))) {
                ipFound = true;
                break;
            }
        }

        Assertions.assertTrue(ipFound, "IP not found in any admin login email body");

    }


    // NEGATIVE TESTS //
    // NEGATIVE TESTS //
    // NEGATIVE TESTS //

    @Test
    public void dieticianLoginNotActiveTest() throws Exception {
        UserRoleDTO.DieticianDTO dieticianDTO = new UserRoleDTO.DieticianDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "dieticianLoginNotActiveTest",
                "dieticianLoginNotActiveTest",
                null,
                null,
                "dieticianLoginNotActiveTest",
                "dieticianLoginNotActiveTest",
                "dieticianLoginNotActiveTest@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null
        );

        String loginRequestJson = """
        {
          "login": "dieticianLoginNotActiveTest",
          "password": "dieticianLoginNotActiveTest"
        }
        """;

        DieticianDTO dieticianDTO2 = new DieticianDTO(dieticianDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(dieticianDTO2);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

//        accountTestHelper.activateAndVerifyByLogin("dieticianLoginNotActiveTest"); //only for tests

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isForbidden())
                .andExpect(result -> Assertions.assertTrue(Objects.requireNonNull(result.getResponse().getErrorMessage()).contains("Account not active")));
    }

    @Test
    public void adminLoginNotActiveTest() throws Exception {
        UserRoleDTO.AdminDTO adminDTO = new UserRoleDTO.AdminDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "adminLoginNotActiveTest",
                "adminLoginNotActiveTest",
                null,
                null,
                "adminLoginNotActiveTest",
                "adminLoginNotActiveTest",
                "adminLoginNotActiveTest@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null
        );

        String loginRequestJson = """
        {
          "login": "adminLoginNotActiveTest",
          "password": "adminLoginNotActiveTest"
        }
        """;

        AdminDTO adminDTO2 = new AdminDTO(adminDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(adminDTO2);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer "+adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
        accountTestHelper.activateByLogin("adminLoginNotActiveTest");
//        accountTestHelper.activateAndVerifyByLogin("adminLoginNotActiveTest"); //only for tests

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isForbidden())
                .andExpect(result -> Assertions.assertTrue(Objects.requireNonNull(result.getResponse().getErrorMessage()).contains("Account not verified")));
    }
}
