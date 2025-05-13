package pl.lodz.p.it.ssbd2025.ssbd02.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;


import static org.mockito.ArgumentMatchers.*;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.ResetPasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.TokenExpiredException;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.PasswordResetTokenService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static reactor.core.publisher.Mono.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOK3 extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PasswordResetTokenService passwordResetTokenService;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;

    @Test
    public void resetPasswordRequest_Success() throws Exception {
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

        resetPasswordDTO = new ResetPasswordDTO("jcheddar@example.com", "P@ssw0rd!");
        json = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(post("/api/account/reset/password/" + capturedToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

//    @Test
//    public void resetPasswordRequest_TokenNotFound() throws Exception {
//        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(
//                "admin@example.com",
//                null
//        );
//
//        doNothing().when(emailService).sendResetPasswordEmail(
//                anyString(), anyString(), any(), tokenCaptor.capture());
//
//        String json = objectMapper.writeValueAsString(resetPasswordDTO);
//
//        mockMvc.perform(post("/api/account/reset/password/request")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk());
//
//        verify(emailService, times(1)).sendResetPasswordEmail(
//                eq("admin@example.com"),
//                anyString(),
//                any(),
//                anyString()
//        );
//
//        String capturedToken = (tokenCaptor.getValue()).replace("1", "0");
//
//        resetPasswordDTO = new ResetPasswordDTO("admin@example.com", "P@ssw0rd!");
//        json = objectMapper.writeValueAsString(resetPasswordDTO);
//
//        mockMvc.perform(post("/api/account/reset/password/" + capturedToken )
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isNotFound());
//    }

    @Test
    public void resetPasswordRequest_TokenExpired() throws Exception {
        String email = "admin@example.com";
        String newPassword = "P@ssw0rd!";

        ResetPasswordDTO resetPasswordRequestDTO = new ResetPasswordDTO(email, null);

        doNothing().when(emailService).sendResetPasswordEmail(
                anyString(), anyString(), any(), tokenCaptor.capture());

        String requestJson = objectMapper.writeValueAsString(resetPasswordRequestDTO);

        mockMvc.perform(post("/api/account/reset/password/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(emailService, times(1)).sendResetPasswordEmail(
                eq(email),
                anyString(),
                any(),
                anyString()
        );

        String capturedToken = tokenCaptor.getValue();

        doThrow(new TokenExpiredException()).when(passwordResetTokenService)
                .validatePasswordResetToken(eq(capturedToken));

        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO(email, newPassword);
        String resetJson = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(post("/api/account/reset/password/" + capturedToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson))
                .andExpect(status().isUnauthorized());

    }
}

