package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.LoginDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ResetPasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.TokenPairDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK9Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountTestHelper accountTestHelper;

    @MockitoBean
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;

    @Captor
    private ArgumentCaptor<String> passwordCaptor;

    String clientUUID = "00000000-0000-0000-0000-000000000005";

    String wrongUUID = "21370000-0000-0000-0000-000000000005";


    public String loginAsAdmin() throws Exception {
        accountTestHelper.setPassword("jcheddar", "P@ssw0rd!");
        LoginDTO loginDTO = new LoginDTO(
                "jcheddar",
                "P@ssw0rd!"
        );

        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("value").asText();
    }

    public void logout(String token) throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk());
    }

    @Order(1)
    @Test
    public void resetPasswordByAdmin_Success() throws Exception {
        String adminAccessToken = loginAsAdmin();
        accountTestHelper.setPassword("agorgonzola", "P@ssw0rd!");
        accountTestHelper.checkPassword("agorgonzola", "P@ssw0rd!");
        doNothing().when(emailService).sendPasswordChangedByAdminEmail(anyString(), anyString(), any(), tokenCaptor.capture(), anyString());
        mockMvc.perform(post("/api/account/" + clientUUID + "/changePassword")
                .header("Authorization", "Bearer " + adminAccessToken)
        ).andExpect(status().isOk());
        String token = tokenCaptor.getValue();
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(
                null,
                "P@ssw0rd!!"
        );
        String json = objectMapper.writeValueAsString(resetPasswordDTO);
        mockMvc.perform(post("/api/account/reset/password/" + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isOk());
        logout(adminAccessToken);
        accountTestHelper.checkPassword("agorgonzola", "P@ssw0rd!!");
    }

    @Order(2)
    @Test
    public void resetPasswordByNonAdminUser_AccessDenied() throws Exception {
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

        doNothing().when(emailService).sendPasswordChangedByAdminEmail(anyString(), anyString(), any(), anyString(), anyString());
        mockMvc.perform(post("/api/account/" + clientUUID + "/changePassword")
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isUnauthorized());
    }

    @Order(3)
    @Test
    public void resetPasswordByGuest() throws Exception {
        doNothing().when(emailService).sendPasswordChangedByAdminEmail(anyString(), anyString(), any(), anyString(), anyString());
        mockMvc.perform(post("/api/account/" + clientUUID + "/changePassword")
        ).andExpect(status().isUnauthorized());
    }

    @Order(4)
    @Test
    public void changePasswordByAdmin_AccountNotFound() throws Exception {
        String adminAccessToken = loginAsAdmin();
        doNothing().when(emailService).sendPasswordChangedByAdminEmail(anyString(), anyString(), any(), tokenCaptor.capture(), anyString());
        mockMvc.perform(post("/api/account/" + wrongUUID + "/changePassword")
                .header("Authorization", "Bearer " + adminAccessToken)
        ).andExpect(status().isNotFound());
        logout(adminAccessToken);
    }

    @Order(5)
    @Test
    public void changePasswordByAdmin_LoginByGeneratedPassword() throws Exception {
        String adminAccessToken = loginAsAdmin();
        doNothing().when(emailService).sendPasswordChangedByAdminEmail(
                anyString(), anyString(), any(), tokenCaptor.capture(), passwordCaptor.capture()
        );
        mockMvc.perform(post("/api/account/" + clientUUID + "/changePassword")
                .header("Authorization", "Bearer " + adminAccessToken)
        ).andExpect(status().isOk());
        logout(adminAccessToken);

        LoginDTO loginDTO = new LoginDTO(
                "agorgonzola",
                passwordCaptor.getValue()
        );
        String json = objectMapper.writeValueAsString(loginDTO);

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }



}
