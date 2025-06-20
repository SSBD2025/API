package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateAccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK13Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LockTokenService lockTokenService;

    @Autowired
    private AccountTestHelper accountTestHelper;

    private final String accountId = "00000000-0000-0000-0000-000000000001";
    private final Long version = 0L;

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

    @Test
    void shouldUpdateAccountSuccessfully() throws Exception {
        String accountId2 = "00000000-0000-0000-0000-000000000007";

        given(lockTokenService.verifyToken("test-token"))
                .willReturn(new LockTokenService.Record<UUID, Long>(UUID.fromString(accountId2), version));
        UpdateAccountDTO updateDto = new UpdateAccountDTO("Joe", "Doe", "test-token");

        mockMvc.perform(put("/api/account/" + accountId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNoContent());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId2));
        Assertions.assertEquals("Joe", account.getFirstName());
        Assertions.assertEquals("Doe", account.getLastName());
    }

    @Test
    void shouldReturnBadRequestForInvalidUUID() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "token");

        mockMvc.perform(put("/api/account/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void shouldReturnForbiddenWithoutAdminRole() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "token");

        String loginRequestJson = """
                {
                  "login": "agorgonzola",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk()).andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token2 = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token2)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token2)).andReturn();
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "token");

        mockMvc.perform(put("/api/account/" + randomUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    void shouldReturn400ForInvalidUuidFormat() throws Exception {
        mockMvc.perform(put("/api/account/" + "bajo-jajo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    void shouldReturn400ForMissingFirstName() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO(null, "Doe", "token");

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    void shouldReturn400ForMissingLastName() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("Joe", null, "token");

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    void shouldReturn400ForMissingLockToken() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("Joe", "Doe", null);

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    void shouldReturn400ForTooLongFirstName() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("Aureliusz Zygmunt Konstantynowicz Księżniczysławowicz Władysławowicz", "Doe", "token");

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    void shouldReturn400ForTooLongLastName() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("Joe", "Szczepanowski-Wielkopolski-Piastowskiewicz-Herniałkowski-Mieczysławowicz", "some-token");

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }
}
