package pl.lodz.p.it.ssbd2025.ssbd02.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.*;
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


import static org.mockito.ArgumentMatchers.*;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.ResetPasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK3Test extends BaseIntegrationTest {

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

    @Test
    public void resetPasswordRequest_Success() throws Exception {
        accountTestHelper.setPassword("jcheddar", "P@ssw0rd!");
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(
                "jcheddar@example.com",
                null
        );

        doNothing().when(emailService).sendResetPasswordEmail(
                anyString(), anyString(), any(), tokenCaptor.capture());

        String json = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(post("/api/account/reset/password/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(emailService, times(1)).sendResetPasswordEmail(
                eq("jcheddar@example.com"),
                anyString(),
                any(),
                anyString()
        );

        String capturedToken = tokenCaptor.getValue();

        resetPasswordDTO = new ResetPasswordDTO(null, "P@ssw0rd?");
        json = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(post("/api/account/reset/password/" + capturedToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        accountTestHelper.checkPassword("jcheddar", "P@ssw0rd?");
    }

    @Test
    public void resetPassword_TokenNotFound() throws Exception {
        String wrongToken = "21370000-0000-0000-0000-000000000000";

        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(null, "P@ssw0rd!");
        String json = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(post("/api/account/reset/password/" + wrongToken )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    public void resetPasswordRequest_TokenExpired() throws Exception {
//        accountTestHelper.setPassword("jcheddar", "P@ssw0rd!");
//        String email = "jcheddar@example.com";
//        String newPassword = "P@ssw0rd?";
//
//        ResetPasswordDTO resetPasswordRequestDTO = new ResetPasswordDTO(email, null);
//
//        doNothing().when(emailService).sendResetPasswordEmail(
//                anyString(), anyString(), any(), tokenCaptor.capture());
//
//        String requestJson = objectMapper.writeValueAsString(resetPasswordRequestDTO);
//
//        mockMvc.perform(post("/api/account/reset/password/request")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isOk());
//
//        verify(emailService, times(1)).sendResetPasswordEmail(
//                eq(email),
//                anyString(),
//                any(),
//                anyString()
//        );
//
//        String capturedToken = tokenCaptor.getValue();
//
//        doThrow(new TokenExpiredException()).when(passwordResetTokenService)
//                .validatePasswordResetToken(eq(capturedToken));
//
//        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(email, newPassword);
//        String resetJson = objectMapper.writeValueAsString(resetPasswordDTO);
//
//        mockMvc.perform(post("/api/account/reset/password/" + capturedToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(resetJson))
//                .andExpect(status().isUnauthorized());
//
//        accountTestHelper.checkPassword("jcheddar", "P@ssw0rd!");
//    }

    @Test
    public void resetPasswordRequest_AccountNotFound() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(
                "example.wrong.mail@example.com",
                null
        );


        String json = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(post("/api/account/reset/password/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }


    @Test
    public void password_NotNull() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(
                "jcheddar@example.com",
                "P@ssw0rd!"
        );
        String json = objectMapper.writeValueAsString(resetPasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/reset/password/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest()).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("{\"violations\":[{\"fieldName\":\"password\",\"message\":\"must be null\"}]}"));
    }

    @Test
    public void email_NotEmailFormat() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(
                "ssbd.p.lodz.pl",
                null
        );
        String json = objectMapper.writeValueAsString(resetPasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/reset/password/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest()).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("{\"violations\":[{\"fieldName\":\"email\",\"message\":\"must be a well-formed email address\"}]}"));
    }

    @Test
    public void email_Null() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(
                null,
                null
        );
        String json = objectMapper.writeValueAsString(resetPasswordDTO);

        MvcResult result = mockMvc.perform(post("/api/account/reset/password/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest()).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains("{\"violations\":[{\"fieldName\":\"email\",\"message\":\"must not be null\"}]}"));
    }

}

