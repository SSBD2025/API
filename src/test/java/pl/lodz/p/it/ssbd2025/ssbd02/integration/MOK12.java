package pl.lodz.p.it.ssbd2025.ssbd02.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.UpdateAccountDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.AccountRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.LockTokenService;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOK12 extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    private LockTokenService lockTokenService;

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
        accessToken = objectMapper.readTree(responseJson).get("accessToken").asText();
    }

    @Test
    void shouldUpdateAccountSuccessfully() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000005";

        given(lockTokenService.verifyToken("sometoken"))
                .willReturn(new LockTokenService.Record<UUID, Long>(UUID.fromString(accountId), 0L));

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnForbiddenForAnonymous() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        String accountId = "00000000-0000-0000-0000-000000000005";

        given(lockTokenService.verifyToken("sometoken"))
                .willReturn(new LockTokenService.Record<UUID, Long>(UUID.fromString(accountId), 0L));

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + "bajo-jajo"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400ForMissingFirstName() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO(null, "Doe", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForMissingLastName() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", null, "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForMissingLockToken() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Doe", null);

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForTooLongFirstName() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("Aureliusz Zygmunt Konstantynowicz Księżniczysławowicz Władysławowicz", "Doe", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400ForTooLongLastName() throws Exception {
        UpdateAccountDTO dto = new UpdateAccountDTO("John", "Szczepanowski-Wielkopolski-Piastowskiewicz-Herniałkowski-Mieczysławowicz", "sometoken");

        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/account/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isBadRequest());
    }
}
