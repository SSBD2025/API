package pl.lodz.p.it.ssbd2025.ssbd02.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.LoginDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.TokenPairDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK8Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountTestHelper accountTestHelper;

    private String loginAsUser() throws Exception {
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
        String responseBody = result.getResponse().getContentAsString();
        TokenPairDTO tokenPair = objectMapper.readValue(responseBody, TokenPairDTO.class);
        return tokenPair.accessToken();
    }

    public void logout(String token) throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk());
    }


    @Test
    public void changePassword_Success() throws Exception {
        accountTestHelper.setPassword("drice", "P@ssw0rd!");
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

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0rd!",
                "P@ssw0rd?"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        mockMvc.perform(post("/api/account/changePassword")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChangePassword))
                .andExpect(status().isOk());

        accountTestHelper.checkPassword("drice", "P@ssw0rd?");
    }


    @Test
    public void changePassword_Unauthorized() throws Exception {


        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0rd!",
                "P@ssw0rd?"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        mockMvc.perform(post("/api/account/changePassword")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonChangePassword))
                .andExpect(status().isForbidden());

    }


    @Test
    public void changePassword_InvalidCredentials() throws Exception {

        LoginDTO loginDTO = new LoginDTO(
                "agorgonzola",
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

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "wrongCredentials",
                "P@ssw0rd?"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isUnauthorized());

        accountTestHelper.checkPassword("agorgonzola", "P@ssw0rd!");
    }


    @Test
    public void oldPassword_tooShort() throws Exception {
        String userAccessToken = loginAsUser();

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0r",
                "P@ssw0rd?"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isBadRequest()).andReturn();
        logout(userAccessToken);
        String responseBody = result.getResponse().getContentAsString();
//        Assertions.assertTrue(responseBody.contains("\"fieldName\": \"oldPassword\","));
        Assertions.assertTrue(responseBody.contains("size must be between 8 and 60"));
    }

    @Test
    public void oldPassword_tooLong() throws Exception {
        String userAccessToken = loginAsUser();

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has",
                "P@ssw0rd?"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isBadRequest()).andReturn();
        logout(userAccessToken);
        String responseBody = result.getResponse().getContentAsString();
//        Assertions.assertTrue(responseBody.contains("\"fieldName\": \"oldPassword\","));
        Assertions.assertTrue(responseBody.contains("size must be between 8 and 60"));
    }

    @Test
    public void newPassword_tooShort() throws Exception {
        String userAccessToken = loginAsUser();

        //Too short
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0rd!",
                "abcdefg"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isBadRequest()).andReturn();
        logout(userAccessToken);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("size must be between 8 and 60"));
    }

    @Test
    public void newPassword_tooLong() throws Exception {
        String userAccessToken = loginAsUser();

        //Too long
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0rd!",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isBadRequest()).andReturn();
        logout(userAccessToken);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("size must be between 8 and 60"));
    }


    @Test
    public void newPassword_UppercaseLetter_Digit_SpecialChar_Missing() throws Exception {
        String userAccessToken = loginAsUser();

        //Uppercase letter, digit and special character missing
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0rd!",
                "password"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isBadRequest()).andReturn();
        logout(userAccessToken);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("{\"violations\":[{\"fieldName\":\"newPassword\",\"message\":\"Password must contain a lowercase letter, an uppercase letter, a digit, and a special character.\"}]}"));
    }

    @Test
    public void newPassword_Digit_SpecialChar_Missing() throws Exception {
        String userAccessToken = loginAsUser();

        //Digit and special character missing
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0rd!",
                "Password"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isBadRequest()).andReturn();
        logout(userAccessToken);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("{\"violations\":[{\"fieldName\":\"newPassword\",\"message\":\"Password must contain a lowercase letter, an uppercase letter, a digit, and a special character.\"}]}"));
    }

    @Test
    public void newPassword_Digit_Missing() throws Exception {
        String userAccessToken = loginAsUser();

        //Digit missing
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0rd!",
                "P@ssword"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isBadRequest()).andReturn();
        logout(userAccessToken);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("{\"violations\":[{\"fieldName\":\"newPassword\",\"message\":\"Password must contain a lowercase letter, an uppercase letter, a digit, and a special character.\"}]}"));
    }

    @Test
    public void newPassword_SpecialChar_Missing() throws Exception {
        String userAccessToken = loginAsUser();

        //Special character missing
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "P@ssw0rd!",
                "Passw0rd"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isBadRequest()).andReturn();
        logout(userAccessToken);
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("{\"violations\":[{\"fieldName\":\"newPassword\",\"message\":\"Password must contain a lowercase letter, an uppercase letter, a digit, and a special character.\"}]}"));
    }
}
