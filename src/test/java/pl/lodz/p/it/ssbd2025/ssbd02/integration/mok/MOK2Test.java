package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper.extractTextFromMimeMessage;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
public class MOK2Test extends BaseIntegrationTest { //REGISTER
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mail.verify.url}")
    private String verificationURL;

    @Autowired
    private TokenUtil tokenUtil;

    private String token;
    @Autowired
    private AccountTestHelper accountTestHelper;

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
        token = objectMapper.readTree(responseJson).get("value").asText();

        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }
    // POSITIVE TESTS //
    // POSITIVE TESTS //
    // POSITIVE TESTS //

    @Test
    public void registerClientTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                "Joe",
                "Doe",
                "doe.joe@example.com",
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

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.login").value("testuser"))
                .andExpect(jsonPath("$.account.firstName").value("Joe"))
                .andExpect(jsonPath("$.account.verified").value(false))
                .andExpect(jsonPath("$.account.active").value(true));

        Assertions.assertNotNull(accountTestHelper.getClientByLogin("testuser"));
    }

    @Test
    public void registerDieticianTest() throws Exception {
        UserRoleDTO.DieticianDTO dieticianDTO = new UserRoleDTO.DieticianDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "dietician",
                "P@ssw0rd!",
                null,
                null,
                "Doe",
                "Joe",
                "joe.doe@example.com",
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

        DieticianDTO dieticianDTO1 = new DieticianDTO(dieticianDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(dieticianDTO1);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.login").value("dietician"))
                .andExpect(jsonPath("$.account.firstName").value("Doe"))
                .andExpect(jsonPath("$.account.verified").value(false))
                .andExpect(jsonPath("$.account.active").value(false));

        Assertions.assertNotNull(accountTestHelper.getClientByLogin("testuser"));
    }

    @Test
    public void registerAdminTest() throws Exception {
        UserRoleDTO.AdminDTO adminDTO = new UserRoleDTO.AdminDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "newadmin",
                "P@ssw0rd!",
                null,
                null,
                "doejoe",
                "joedoe",
                "doejoe.joedoe@example.com",
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

        AdminDTO adminDTO1 = new AdminDTO(adminDTO, accountDTO);
        String requestJson = objectMapper.writeValueAsString(adminDTO1);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.login").value("newadmin"))
                .andExpect(jsonPath("$.account.firstName").value("doejoe"))
                .andExpect(jsonPath("$.account.verified").value(false))
                .andExpect(jsonPath("$.account.active").value(false));

        Assertions.assertNotNull(accountTestHelper.getClientByLogin("testuser"));
    }

    // NEGATIVE TESTS //
    // NEGATIVE TESTS //
    // NEGATIVE TESTS //

    @Test
    public void duplicateRegisterClientValidationDuplicateLoginErrorTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstclient",
                "P@ssw0rd!",
                null,
                null,
                "firstclient",
                "firstclient",
                "firstclient@example.com",
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

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        UserRoleDTO.ClientDTO clientDTO3 = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO4 = new AccountDTO(
                null,
                null,
                "firstclient",
                "P@ssw0rd!",
                null,
                null,
                "secondclient",
                "secondclient",
                "secondclient@example.com",
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

        ClientDTO clientDTO5 = new ClientDTO(clientDTO3, accountDTO4);

        String requestJson2 = objectMapper.writeValueAsString(clientDTO5);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("this login is already in use")));
    }

    @Test
    public void duplicateRegisterDieticianValidationDuplicateLoginErrorTest() throws Exception {
        UserRoleDTO.DieticianDTO dieticianDTO = new UserRoleDTO.DieticianDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstdietician",
                "P@ssw0rd!",
                null,
                null,
                "firstdietician",
                "firstdietician",
                "firstdietician@example.com",
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

        DieticianDTO dieticianDTO1 = new DieticianDTO(dieticianDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(dieticianDTO1);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        UserRoleDTO.DieticianDTO dieticianDTO3 = new UserRoleDTO.DieticianDTO();

        AccountDTO accountDTO4 = new AccountDTO(
                null,
                null,
                "firstdietician",
                "P@ssw0rd!",
                null,
                null,
                "seconddietician",
                "seconddietician",
                "seconddietician@example.com",
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

        DieticianDTO dieticianDTO5 = new DieticianDTO(dieticianDTO3, accountDTO4);

        String requestJson2 = objectMapper.writeValueAsString(dieticianDTO5);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("this login is already in use")));
    }

    @Test
    public void duplicateRegisterAdminValidationDuplicateLoginErrorTest() throws Exception {
        UserRoleDTO.AdminDTO adminDTO = new UserRoleDTO.AdminDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstadmin",
                "P@ssw0rd!",
                null,
                null,
                "firstadmin",
                "firstadmin",
                "firstadmin@example.com",
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

        AdminDTO adminDTO1 = new AdminDTO(adminDTO, accountDTO);
        String requestJson = objectMapper.writeValueAsString(adminDTO1);

        String loginRequestJson = """
        {
          "login": "adminlogin",
          "password": "P@ssw0rd!"
        }
        """;

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        UserRoleDTO.AdminDTO adminDTO2 = new UserRoleDTO.AdminDTO();

        AccountDTO accountDTO2 = new AccountDTO(
                null,
                null,
                "firstadmin",
                "P@ssw0rd!",
                null,
                null,
                "secondadmin",
                "secondadmin",
                "secondadmin@example.com",
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

        AdminDTO adminDTO3 = new AdminDTO(adminDTO2, accountDTO2);
        String requestJson2 = objectMapper.writeValueAsString(adminDTO3);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("this login is already in use")));
    }



    @Test
    public void duplicateRegisterClientValidationDuplicateEmailErrorTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstclient2",
                "P@ssw0rd!",
                null,
                null,
                "firstclient2",
                "firstclient2",
                "firstclient2@example.com",
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

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        UserRoleDTO.ClientDTO clientDTO3 = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO4 = new AccountDTO(
                null,
                null,
                "secondclient2",
                "P@ssw0rd!",
                null,
                null,
                "secondclient2",
                "secondclient2",
                "firstclient2@example.com",
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

        ClientDTO clientDTO5 = new ClientDTO(clientDTO3, accountDTO4);

        String requestJson2 = objectMapper.writeValueAsString(clientDTO5);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("this email is already in use")));
    }

    @Test
    public void duplicateRegisterDieticianValidationDuplicateEmailErrorTest() throws Exception {
        UserRoleDTO.DieticianDTO dieticianDTO = new UserRoleDTO.DieticianDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstdietician2",
                "P@ssw0rd!",
                null,
                null,
                "firstdietician2",
                "firstdietician2",
                "firstdietician2@example.com",
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

        DieticianDTO dieticianDTO1 = new DieticianDTO(dieticianDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(dieticianDTO1);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        UserRoleDTO.DieticianDTO dieticianDTO3 = new UserRoleDTO.DieticianDTO();

        AccountDTO accountDTO4 = new AccountDTO(
                null,
                null,
                "seconddietician2",
                "P@ssw0rd!",
                null,
                null,
                "seconddietician2",
                "seconddietician2",
                "firstdietician2@example.com",
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

        DieticianDTO dieticianDTO5 = new DieticianDTO(dieticianDTO3, accountDTO4);

        String requestJson2 = objectMapper.writeValueAsString(dieticianDTO5);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("this email is already in use")));
    }

    @Test
    public void duplicateRegisterAdminValidationDuplicateEmailErrorTest() throws Exception {
        UserRoleDTO.AdminDTO adminDTO = new UserRoleDTO.AdminDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstadmin2",
                "P@ssw0rd!",
                null,
                null,
                "firstadmin2",
                "firstadmin2",
                "firstadmin2@example.com",
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

        AdminDTO adminDTO1 = new AdminDTO(adminDTO, accountDTO);
        String requestJson = objectMapper.writeValueAsString(adminDTO1);

        String loginRequestJson = """
        {
          "login": "adminlogin",
          "password": "P@ssw0rd!"
        }
        """;

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        UserRoleDTO.AdminDTO adminDTO2 = new UserRoleDTO.AdminDTO();

        AccountDTO accountDTO2 = new AccountDTO(
                null,
                null,
                "secondadmin2",
                "P@ssw0rd!",
                null,
                null,
                "secondadmin2",
                "secondadmin2",
                "firstadmin2@example.com",
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

        AdminDTO adminDTO3 = new AdminDTO(adminDTO2, accountDTO2);
        String requestJson2 = objectMapper.writeValueAsString(adminDTO3);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("this email is already in use")));
    }

    @Test
    public void verificationMockTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();
        AccountDTO accountDTO = new AccountDTO(
                null, null,
                "testuser2",
                "P@ssw0rd!",
                null, null,
                "Joe2", "Doe2",
                "doe.joe2@example.com",
                null, null,
                Language.pl_PL,
                null, null,
                false,
                false,
                0,
                null
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);
        String requestJson = objectMapper.writeValueAsString(clientDTO2);
        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.login").value("testuser2"))
                .andExpect(jsonPath("$.account.firstName").value("Joe2"))
                .andExpect(jsonPath("$.account.verified").value(false))
                .andExpect(jsonPath("$.account.active").value(true));


        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        verify(mailSender).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();

        String emailBody = extractTextFromMimeMessage(sentMessage);
        Matcher matcher = Pattern.compile("verify\\?token=([a-zA-Z0-9\\-]+)").matcher(emailBody);
        Assertions.assertTrue(matcher.find(), "Token not found in the email body");
        String verifToken = matcher.group(1);

        mockMvc.perform(get("/api/account/verify")
                        .param("token", verifToken))
                .andExpect(status().isOk());

        Account account = accountRepository.findByLogin("testuser2")
                .orElseThrow(() -> new AssertionError("Account not found"));

        Assertions.assertTrue(account.isVerified(), "Account should be verified");
    }
}
