package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateAccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK13Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LockTokenService lockTokenService;

    @Autowired
    private AccountTestHelper accountTestHelper;

    private final String accountId ="00000000-0000-0000-0000-000000000001";
    private final Long version = 0L;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAccountSuccessfully() throws Exception {
        String accountId2 = "00000000-0000-0000-0000-000000000007";

        given(lockTokenService.verifyToken("test-token"))
                .willReturn(new LockTokenService.Record<UUID, Long>(UUID.fromString(accountId2), version));
        UpdateAccountDTO updateDto = new UpdateAccountDTO("Joe", "Doe", "test-token");

        mockMvc.perform(put("/api/account/" + accountId2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId2));
        Assertions.assertEquals("Joe", account.getFirstName());
        Assertions.assertEquals("Doe", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidUUID() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "token");

        mockMvc.perform(put("/api/account/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
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

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "token");

        mockMvc.perform(put("/api/account/" + randomUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400ForInvalidUuidFormat() throws Exception {
        mockMvc.perform(put("/api/account/" + "bajo-jajo"))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400ForMissingFirstName() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO(null, "Doe", "token");

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400ForMissingLastName() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("Joe", null, "token");

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400ForMissingLockToken() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("Joe", "Doe", null);

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400ForTooLongFirstName() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("Aureliusz Zygmunt Konstantynowicz Księżniczysławowicz Władysławowicz", "Doe", "token");

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400ForTooLongLastName() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAccountDTO dto = new UpdateAccountDTO("Joe", "Szczepanowski-Wielkopolski-Piastowskiewicz-Herniałkowski-Mieczysławowicz", "some-token");

        mockMvc.perform(put("/api/account/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        Account account = accountTestHelper.getClientById(UUID.fromString(accountId));
        Assertions.assertEquals("Justin", account.getFirstName());
        Assertions.assertEquals("Cheddar", account.getLastName());
    }
}
