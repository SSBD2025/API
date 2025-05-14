package pl.lodz.p.it.ssbd2025.ssbd02.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.*;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.TokenEntity;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.repository.TokenRepository;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.AccountService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK10Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AccountService accountService;

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
    }

    @Test
    public void changeOwnEmailPositiveTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        TokenPairDTO tokenPair = objectMapper.readValue(responseBody, TokenPairDTO.class);

        String accessToken = tokenPair.accessToken();

        ChangeEmailDTO changeEmailDTO = new ChangeEmailDTO("newemail@example.com");

        String changeEmailJson = objectMapper.writeValueAsString(changeEmailDTO);

        mockMvc.perform(post("/api/account/change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeEmailJson))
                .andExpect(status().isOk());

        TokenEntity token = tokenRepository.findAll().stream()
                .filter(t -> t.getType() == TokenType.EMAIL_CHANGE)
                .findFirst()
                .orElseThrow();

        Assertions.assertNotNull(token);
        Assertions.assertEquals(TokenType.EMAIL_CHANGE, token.getType());
        Assertions.assertEquals("newemail@example.com", jwtTokenProvider.getNewEmailFromToken(token.getToken()));

        mockMvc.perform(get("/api/account/confirm-email")
                        .param("token", token.getToken()))
                .andExpect(status().isOk());

        Assertions.assertEquals("newemail@example.com", accountService.getAccountByLogin("drice").account().email());
    }

    @Test
    @WithMockUser
    public void changeOwnEmailNewEmailTooLongTest() throws Exception {
        ChangeEmailDTO dto = new ChangeEmailDTO("verylongemailaddress1234567890verylongemailaddress1234567890verylongemailaddress1234567890@example.com");
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/account/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void changeOwnEmailWhenEmailInvalidFormatTest() throws Exception {
        ChangeEmailDTO dto = new ChangeEmailDTO("not_an_email");

        mockMvc.perform(post("/api/account/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void changeOwnEmailUnauthorizedTest() throws Exception {
        ChangeEmailDTO changeEmailDTO = new ChangeEmailDTO("newemail@example.com");

        String changeEmailJson = objectMapper.writeValueAsString(changeEmailDTO);

        mockMvc.perform(post("/api/account/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeEmailJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void changeOwnEmailEmailIsBlankTest() throws Exception {
        ChangeEmailDTO dto = new ChangeEmailDTO("");

        mockMvc.perform(post("/api/account/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void changeOwnEmailSameEmailTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO("tcheese", "P@ssw0rd!");
        String loginJson = objectMapper.writeValueAsString(loginDTO);

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        TokenPairDTO tokenPair = objectMapper.readValue(loginResult.getResponse().getContentAsString(), TokenPairDTO.class);
        String accessToken = tokenPair.accessToken();

        ChangeEmailDTO sameEmail = new ChangeEmailDTO("tcheese@example.com");
        String json = objectMapper.writeValueAsString(sameEmail);

        mockMvc.perform(post("/api/account/change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    public void changeOwnEmailEmailAlreadyInUseNegativeTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );
        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        TokenPairDTO tokenPair = objectMapper.readValue(loginResult.getResponse().getContentAsString(), TokenPairDTO.class);
        String accessToken = tokenPair.accessToken();

        ChangeEmailDTO takenEmail = new ChangeEmailDTO("jcheddar@example.com");
        String takenEmailJson = objectMapper.writeValueAsString(takenEmail);

        mockMvc.perform(post("/api/account/change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(takenEmailJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void revertEmailChangePositiveTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "tcheese",
                "P@ssw0rd!"
        );

        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        TokenPairDTO tokenPair = objectMapper.readValue(responseBody, TokenPairDTO.class);

        String accessToken = tokenPair.accessToken();

        ChangeEmailDTO changeEmailDTO = new ChangeEmailDTO("changeEmail@example.com");

        String changeEmailJson = objectMapper.writeValueAsString(changeEmailDTO);

        mockMvc.perform(post("/api/account/change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeEmailJson))
                .andExpect(status().isOk());

        TokenEntity token = tokenRepository.findAll().stream()
                .filter(t -> t.getType() == TokenType.EMAIL_CHANGE)
                .findFirst()
                .orElseThrow();

        Assertions.assertNotNull(token);
        Assertions.assertEquals(TokenType.EMAIL_CHANGE, token.getType());
        Assertions.assertEquals("changeEmail@example.com", jwtTokenProvider.getNewEmailFromToken(token.getToken()));

        mockMvc.perform(get("/api/account/confirm-email")
                        .param("token", token.getToken()))
                .andExpect(status().isOk());

        TokenEntity revertToken = tokenRepository.findAll().stream()
                .filter(t -> t.getType() == TokenType.EMAIL_REVERT)
                .findFirst()
                .orElseThrow();

        Assertions.assertNotNull(revertToken);
        Assertions.assertEquals(TokenType.EMAIL_REVERT, revertToken.getType());

        mockMvc.perform(get("/api/account/revert-email-change")
                        .param("token", revertToken.getToken()))
                .andExpect(status().isOk());

        Assertions.assertEquals("tcheese@example.com", accountService.getAccountByLogin(loginDTO.getLogin()).account().email());
    }

    @Test
    public void revertEmailChangeTokenNotFoundTest() throws Exception {
        String notFoundToken = "notFoundToken";

        mockMvc.perform(get("/api/account/revert-email-change")
                        .param("token", notFoundToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void resendEmailChangeLinkPositiveTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "drice",
                "P@ssw0rd!"
        );

        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        TokenPairDTO tokenPair = objectMapper.readValue(responseBody, TokenPairDTO.class);

        String accessToken = tokenPair.accessToken();

        ChangeEmailDTO changeEmailDTO = new ChangeEmailDTO("adminnewemail@example.com");

        String changeEmailJson = objectMapper.writeValueAsString(changeEmailDTO);

        mockMvc.perform(post("/api/account/change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeEmailJson))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/account/resend-change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void resendEmailChangeLinkUnauthorizedTest() throws Exception {
        mockMvc.perform(post("/api/account/resend-change-email")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void resendEmailChangeLinkTokenNotFoundTest() throws Exception {
        LoginDTO loginDTO = new LoginDTO(
                "tcheese",
                "P@ssw0rd!"
        );

        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        TokenPairDTO tokenPair = objectMapper.readValue(responseBody, TokenPairDTO.class);

        String accessToken = tokenPair.accessToken();

        mockMvc.perform(post("/api/account/resend-change-email")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
