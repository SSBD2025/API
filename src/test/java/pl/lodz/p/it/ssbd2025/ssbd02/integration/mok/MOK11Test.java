package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangeEmailDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.LoginDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK11Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    private String accessToken;

    @AfterEach
    void tearDown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + accessToken)).andReturn();
    }

    @Test
    public void changeUserEmailPositiveTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String loginJson = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        accessToken = objectMapper.readTree(responseBody).get("value").asText();

        ChangeEmailDTO dto = new ChangeEmailDTO("newemail@example.com");
        String json = objectMapper.writeValueAsString(dto);

        doNothing().when(emailService).sendChangeEmail(anyString(), anyString(), any(), any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/{id}/change-user-email", "00000000-0000-0000-0000-000000000003")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());
    }

    @Test
    public void changeUserEmailWhenEmailIsBlankTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String loginJson = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        accessToken = objectMapper.readTree(responseBody).get("value").asText();

        ChangeEmailDTO dto = new ChangeEmailDTO("");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/{id}/change-user-email", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void changeUserEmailWhenEmailInvalidFormatTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String loginJson = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        accessToken = objectMapper.readTree(responseBody).get("value").asText();

        ChangeEmailDTO dto = new ChangeEmailDTO("not_a_valid_email");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/{id}/change-user-email", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void changeUserEmailWithNoBody() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String loginJson = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        accessToken = objectMapper.readTree(responseBody).get("value").asText();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/{id}/change-user-email", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void changeUserEmailNewEmailTooLongTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String loginJson = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        accessToken = objectMapper.readTree(responseBody).get("value").asText();

        ChangeEmailDTO dto = new ChangeEmailDTO("verylongemailaddress1234567890verylongemailaddress1234567890verylongemailaddress1234567890@example.com");
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/{id}/change-user-email", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest());
    }

    @Test
    public void changeUserEmailUnauthorizedTest() throws Exception {
        ChangeEmailDTO dto = new ChangeEmailDTO("newemail@example.com");
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/{id}/change-user-email", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isUnauthorized());
    }

    @Test
    public void changeUserEmailUserDoesNotExistTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String loginJson = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        accessToken = objectMapper.readTree(responseBody).get("value").asText();

        ChangeEmailDTO dto = new ChangeEmailDTO("newemail@example.com");
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/{id}/change-user-email", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isNotFound());
    }

    @Test
    public void changeUserEmailWhenEmailAlreadyInUseTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String loginJson = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        accessToken = objectMapper.readTree(responseBody).get("value").asText();

        ChangeEmailDTO dto = new ChangeEmailDTO("drice@example.com");
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/{id}/change-user-email", "00000000-0000-0000-0000-000000000005")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isConflict());
    }
}
