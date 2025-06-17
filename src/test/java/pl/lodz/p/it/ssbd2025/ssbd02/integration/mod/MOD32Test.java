package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.AsyncTestConfig;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(AsyncTestConfig.class)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD32Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    String dieticianId = "00000000-0000-0000-0000-000000000004";

    @Test
    public void assignDieticianTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "jorzel",
                  "password": "P@ssw0rd!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(post("/api/mod/clients/assign-dietician/{id}", dieticianId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void sameDieticianAlreadyAssignedTest() throws Exception {
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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        MvcResult result = mockMvc.perform(post("/api/mod/clients/assign-dietician/{id}", dieticianId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("same_dietician_already_assigned", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void dieticianAlreadyAssignedTest() throws Exception {
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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        MvcResult result = mockMvc.perform(post("/api/mod/clients/assign-dietician/00000000-0000-0000-0000-000000000009")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("dietician_already_assigned", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void notExistingDieticianAssignTest() throws Exception {
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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        MvcResult result = mockMvc.perform(post("/api/mod/clients/assign-dietician/00000000-0000-0000-0000-000000000111")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn();

        Assertions.assertEquals("dietician_not_found", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void assignDieticianUnauthorizedTest() throws Exception {
        mockMvc.perform(post("/api/mod/clients/assign-dietician/00000000-0000-0000-0000-000000000111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()).andReturn();
    }
}
