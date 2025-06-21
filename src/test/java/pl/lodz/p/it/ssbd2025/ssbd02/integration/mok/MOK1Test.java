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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.TokenUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
@Import(AsyncTestConfig.class)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mokTransactionManager")
public class MOK1Test extends BaseIntegrationTest { //REGISTER
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
    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setup() throws Exception {
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.firstName").value("Joe"));

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
                null,
                false
        );

        DieticianDTO dieticianDTO1 = new DieticianDTO(dieticianDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(dieticianDTO1);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.firstName").value("Doe"));

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
                null,
                false
        );

        AdminDTO adminDTO1 = new AdminDTO(adminDTO, accountDTO);
        String requestJson = objectMapper.writeValueAsString(adminDTO1);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.firstName").value("doejoe"));

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
                null,
                false
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
                null,
                false
        );

        ClientDTO clientDTO5 = new ClientDTO(clientDTO3, accountDTO4);

        String requestJson2 = objectMapper.writeValueAsString(clientDTO5);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict());
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
                null,
                false
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
                null,
                false
        );

        DieticianDTO dieticianDTO5 = new DieticianDTO(dieticianDTO3, accountDTO4);

        String requestJson2 = objectMapper.writeValueAsString(dieticianDTO5);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict());
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
                null,
                false
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
                null,
                false
        );

        AdminDTO adminDTO3 = new AdminDTO(adminDTO2, accountDTO2);
        String requestJson2 = objectMapper.writeValueAsString(adminDTO3);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict());
    }


    @Test
    public void duplicateRegisterClientValidationDuplicateEmailErrorTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstclientt",
                "P@ssw0rd!",
                null,
                null,
                "firstclientt",
                "firstclientt",
                "firstclientt@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
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
                "secondclientt",
                "P@ssw0rd!",
                null,
                null,
                "secondclientt",
                "secondclientt",
                "firstclientt@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO5 = new ClientDTO(clientDTO3, accountDTO4);

        String requestJson2 = objectMapper.writeValueAsString(clientDTO5);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict());
    }

    @Test
    public void duplicateRegisterDieticianValidationDuplicateEmailErrorTest() throws Exception {
        UserRoleDTO.DieticianDTO dieticianDTO = new UserRoleDTO.DieticianDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstdieticiann",
                "P@ssw0rd!",
                null,
                null,
                "firstdieticiann",
                "firstdieticiann",
                "firstdieticiann@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
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
                "seconddieticiann",
                "P@ssw0rd!",
                null,
                null,
                "seconddieticiann",
                "seconddieticiann",
                "firstdieticiann@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        DieticianDTO dieticianDTO5 = new DieticianDTO(dieticianDTO3, accountDTO4);

        String requestJson2 = objectMapper.writeValueAsString(dieticianDTO5);

        mockMvc.perform(post("/api/dietician/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict());
    }

    @Test
    public void duplicateRegisterAdminValidationDuplicateEmailErrorTest() throws Exception {
        UserRoleDTO.AdminDTO adminDTO = new UserRoleDTO.AdminDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "firstadminn",
                "P@ssw0rd!",
                null,
                null,
                "firstadminn",
                "firstadminn",
                "firstadminn@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
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
                "secondadminn",
                "P@ssw0rd!",
                null,
                null,
                "secondadminn",
                "secondadminn",
                "firstadminn@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        AdminDTO adminDTO3 = new AdminDTO(adminDTO2, accountDTO2);
        String requestJson2 = objectMapper.writeValueAsString(adminDTO3);

        mockMvc.perform(post("/api/admin/register")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isConflict());
    }

    @Test
    public void verificationMockTest() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();
        AccountDTO accountDTO = new AccountDTO(
                null, null,
                "testuser2",
                "P@ssw0rd!",
                null, null,
                "Joee", "Doee",
                "doe.joe2@example.com",
                null, null,
                Language.pl_PL,
                null, null,
                false,
                false,
                0,
                null,
                false
        );

        ArgumentCaptor<SensitiveDTO> tokenCaptor = ArgumentCaptor.forClass(SensitiveDTO.class);

        doNothing().when(emailService).sendActivationMail(
                anyString(), anyString(), any(), any(), tokenCaptor.capture());

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);
        String requestJson = objectMapper.writeValueAsString(clientDTO2);
        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.firstName").value("Joee"));


        mockMvc.perform(get("/api/account/verify")
                        .param("token", tokenCaptor.getValue().getValue()))
                .andExpect(status().isNoContent());


        Account account = accountRepository.findByLogin("testuser2")
                .orElseThrow(() -> new AssertionError("Account not found"));

        Assertions.assertTrue(account.isVerified(), "Account should be verified");
    }

    // MALFORMATION TESTS //
    // MALFORMATION TESTS //
    // MALFORMATION TESTS //

    @Test
    public void registerClient_Malformation_Login_TooLong_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testusertestusertestusertestusertestusertestusertestusertestusertestusertestuser",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Login_TooShort_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "tes",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Login_Blank_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "      ",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Login_Null_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                null,
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Password_TooShort_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ss",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Password_TooLong_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!P@ssw0rd!P@ssw0rd!P@ssw0rd!P@ssw0rd!P@ssw0rd!P@ssw0rd!P@ssw0rd!P@ssw0rd!P@ssw0rd!",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Password_Blank_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "      ",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Password_Null_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                null,
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Password_MissingSpecialCharacter_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "Passw0rdd",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Password_MissingNumber_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssword!",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Password_MissingLowercaseLetter_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@SSW0RD!",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Password_MissingUppercaseLetter_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "p@ssw0rd!",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_ID_NotNull_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                UUID.randomUUID(),
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Version_NotNull_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                0L,
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Verified_NotNull_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                true,
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Active_NotNull_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                true,
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Firstname_TooShort_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                "",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Firstname_TooLong_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                "JoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoeJoe",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Firstname_Null_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                null,
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Firstname_Blank_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                "           ",
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
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Lastname_TooShort_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                "Joe",
                "",
                "doe.joe@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Lastname_TooLong_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                "Joe",
                "DoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoeDoe",
                "doe.joe@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Lastname_Null_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                "Joe",
                null,
                "doe.joe@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Lastname_Blank_Test() throws Exception {
        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();

        AccountDTO accountDTO = new AccountDTO(
                null,
                null,
                "testuser",
                "P@ssw0rd!",
                null,
                null,
                "Joe",
                "         ",
                "doe.joe@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Email_Null_Test() throws Exception {
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
                null,
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Email_Blank_Test() throws Exception {
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
                "          ",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Email_Missing_at_Test() throws Exception {
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
                "doe.joeexample.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Email_Missing_dot_Test() throws Exception {
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
                "doe.joe@examplecom",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Email_TooLong_Test() throws Exception {
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
                "doe.joedoe.joedoe.joedoe.joedoe.joedoe.joedoe.joedoe.joedoe.joedoe.joedoe.joedoe.joedoe.joedoe.joe@example.com",
                null,
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_LastSuccessfulLogin_NotNull_Test() throws Exception {
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
                Timestamp.from(Instant.now()),
                null,
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_LastFailedLogin_NotNull_Test() throws Exception {
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
                Timestamp.from(Instant.now()),
                Language.pl_PL,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_Language_Null_Test() throws Exception {
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
                null,
                null,
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_LastSuccessfulIP_NotNull_Test() throws Exception {
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
                "1",
                null,
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registerClient_Malformation_LastFailedIP_NotNull_Test() throws Exception {
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
                "1",
                false,
                false,
                0,
                null,
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void registerClient_Malformation_LockedUntil_Null_Test() throws Exception {
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
                Timestamp.from(Instant.now()),
                false
        );

        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);

        String requestJson = objectMapper.writeValueAsString(clientDTO2);

        mockMvc.perform(post("/api/client/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}
