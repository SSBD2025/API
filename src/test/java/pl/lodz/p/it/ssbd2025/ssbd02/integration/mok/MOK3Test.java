package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;


import static org.mockito.ArgumentMatchers.*;

import pl.lodz.p.it.ssbd2025.ssbd02.dto.ResetPasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
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
    private ArgumentCaptor<SensitiveDTO> tokenCaptor;

    @Test
    @WithMockUser(roles = {"CLIENT"})
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
                .andExpect(status().isNoContent());

        verify(emailService, times(1)).sendResetPasswordEmail(
                eq("jcheddar@example.com"),
                anyString(),
                any(),
                any()
        );

        String capturedToken = tokenCaptor.getValue().getValue();

        resetPasswordDTO = new ResetPasswordDTO(null, "P@ssw0rd?");
        json = objectMapper.writeValueAsString(resetPasswordDTO);

        mockMvc.perform(post("/api/account/reset/password/" + capturedToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent());

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
                .andExpect(status().isNoContent());
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
    }

}

