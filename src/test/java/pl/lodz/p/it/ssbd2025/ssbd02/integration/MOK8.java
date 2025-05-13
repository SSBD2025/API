package pl.lodz.p.it.ssbd2025.ssbd02.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.ChangePasswordDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.LoginDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.TokenPairDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOK8 extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Order(1)
    @Test
    public void changePassword_Success() throws Exception {

        LoginDTO loginDTO = new LoginDTO(
                "adminlogin",
                "password"
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
                "password",
                "P@ssw0rd!"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        mockMvc.perform(post("/api/account/changePassword")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonChangePassword))
                .andExpect(status().isOk());
    }


    @Order(2)
    @Test
    public void changePassword_Unauthorized() throws Exception {

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO(
                "password",
                "P@ssw0rd!"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        mockMvc.perform(post("/api/account/changePassword")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonChangePassword))
                .andExpect(status().isForbidden());
    }

    @Order(3)
    @Test
    public void changePassword_InvalidCredentials() throws Exception {

        LoginDTO loginDTO = new LoginDTO(
                "adminlogin",
                "password"
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
                "P@ssw0rd!"
        );

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordDTO);

        mockMvc.perform(post("/api/account/changePassword")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isUnauthorized());
    }

}
