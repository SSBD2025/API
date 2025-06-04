package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK12Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LockTokenService lockTokenService;

    @Autowired
    private AccountTestHelper accountTestHelper;

    private String accessToken;

    @BeforeEach
    void setup() throws Exception {
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

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + accessToken)).andReturn();
    }

    String setupUpdateUser() throws Exception {
        String loginRequestJson = """
        {
          "login": "drice",
          "password": "P@ssw0rd!"
        }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("value").asText();
    }

    @Test
    void shouldUpdateAccountSuccessfully() throws Exception {
        String tok = setupUpdateUser();
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000007";

        given(lockTokenService.verifyToken("sometoken"))
                .willReturn(new LockTokenService.Record<UUID, Long>(UUID.fromString(accountId), 0L));

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tok))
                .andExpect(status().isOk());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("John", account.getFirstName());
        Assertions.assertEquals("Doe", account.getLastName());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + tok)).andReturn();
    }

    @Test
    void shouldReturnUnauthorizedForAnonymous() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000005";

        given(lockTokenService.verifyToken("sometoken"))
                .willReturn(new LockTokenService.Record<UUID, Long>(UUID.fromString(accountId), 0L));

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + "bajo-jajo"))
                .andExpect(status().isUnauthorized());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Anthony", account.getFirstName());
        Assertions.assertEquals("Gorgonzola", account.getLastName());
    }

    @Test
    void shouldReturn400ForMissingFirstName() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO(null, "Doe", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000005";

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Anthony", account.getFirstName());
        Assertions.assertEquals("Gorgonzola", account.getLastName());
    }

    @Test
    void shouldReturn400ForMissingLastName() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", null, "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000005";

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Anthony", account.getFirstName());
        Assertions.assertEquals("Gorgonzola", account.getLastName());
    }

    @Test
    void shouldReturn400ForMissingLockToken() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", null);

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000005";

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Anthony", account.getFirstName());
        Assertions.assertEquals("Gorgonzola", account.getLastName());
    }

    @Test
    void shouldReturn400ForTooLongFirstName() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("Aureliusz Zygmunt Konstantynowicz Księżniczysławowicz Władysławowicz", "Doe", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000005";

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Anthony", account.getFirstName());
        Assertions.assertEquals("Gorgonzola", account.getLastName());
    }

    @Test
    void shouldReturn400ForTooLongLastName() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Szczepanowski-Wielkopolski-Piastowskiewicz-Herniałkowski-Mieczysławowicz", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000005";

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Anthony", account.getFirstName());
        Assertions.assertEquals("Gorgonzola", account.getLastName());
    }
}
