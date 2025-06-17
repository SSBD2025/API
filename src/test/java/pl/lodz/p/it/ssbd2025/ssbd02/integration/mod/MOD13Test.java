package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.LoginDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOD13Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setup() throws Exception {
        LoginDTO loginDTO = new LoginDTO("agorgonzola", "P@ssw0rd!");
        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk())
            .andReturn();
        String responseJson = loginResult.getResponse().getContentAsString();
        token = objectMapper.readTree(responseJson).get("value").asText();
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void getClientBloodTestReport_Client_Success() throws Exception {
        mockMvc.perform(get("/api/mod/blood-test-reports/client")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    public void getClientBloodTestReport_Dietician_Success() throws Exception {
        String uuid = "00000000-0000-0000-0000-000000000005";
        mockMvc.perform(get("/api/mod/blood-test-reports/client/{uuid}", uuid))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    public void getClientBloodTestReport_Client_Forbidden() throws Exception {
        mockMvc.perform(get("/api/mod/blood-test-reports/client"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    public void getClientBloodTestReport_Dietician_Forbidden() throws Exception {
        String uuid = "00000000-0000-0000-0000-000000000005";
        mockMvc.perform(get("/api/mod/blood-test-reports/client/{uuid}", uuid))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getClientBloodTestReport_Client_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/mod/blood-test-reports/client"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getClientBloodTestReport_Dietician_Unauthorized() throws Exception {
        String uuid = "00000000-0000-0000-0000-000000000005";
        mockMvc.perform(get("/api/mod/blood-test-reports/client/{uuid}", uuid))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    public void getClientBloodTestReport_Dietician_AccountNotFound() throws Exception {
        String uuid = "00000000-0000-0000-0000-000000000006";
        MvcResult result = mockMvc.perform(get("/api/mod/blood-test-reports/client/{uuid}", uuid))
                .andExpect(status().isNotFound()).andReturn();
        String errorMessage = result.getResponse().getErrorMessage();
        Assertions.assertEquals("account_not_found", errorMessage);
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    public void getClientBloodTestReport_Dietician_ClientBloodTestReportNotFound() throws Exception {
        String uuid = "00000000-0000-0000-0000-000000000014";
        MvcResult result = mockMvc.perform(get("/api/mod/blood-test-reports/client/{uuid}", uuid))
                .andExpect(status().isNotFound()).andReturn();
        String errorMessage = result.getResponse().getErrorMessage();
        Assertions.assertEquals("client_blood_test_report_not_found", errorMessage);
    }
}