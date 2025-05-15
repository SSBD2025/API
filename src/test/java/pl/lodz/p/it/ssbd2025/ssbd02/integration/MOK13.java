package pl.lodz.p.it.ssbd2025.ssbd02.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.trilead.ssh2.Session;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateAccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.LockTokenService;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static reactor.core.publisher.Mono.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOK13 extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    private LockTokenService lockTokenService;

    private final String accountId ="00000000-0000-0000-0000-000000000001";
    private final Long version = 0L;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateAccountSuccessfully() throws Exception {
        given(lockTokenService.verifyToken("test-token"))
                .willReturn(new LockTokenService.Record<UUID, Long>(UUID.fromString(accountId), version));
        UpdateAccountDTO updateDto = new UpdateAccountDTO("Joe", "Doe", "test-token");

        mockMvc.perform(put("/api/account/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForInvalidUUID() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "token");

        mockMvc.perform(put("/api/account/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
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
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400ForInvalidUuidFormat() throws Exception {
        mockMvc.perform(put("/api/account/" + "bajo-jajo"))
                .andExpect(status().isBadRequest());
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
    }
}
