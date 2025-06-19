package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
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

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    String nineClientsDieticianId = "00000000-0000-0000-0000-000000000004";
    String dieticianId = "00000000-0000-0000-0000-000000000009";

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
                  "login": "kzachod",
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

        MvcResult result2 = mockMvc.perform(post("/api/mod/clients/assign-dietician/{id}", dieticianId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("same_dietician_already_assigned", result2.getResponse().getErrorMessage());

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

        MvcResult result2 = mockMvc.perform(post("/api/mod/clients/assign-dietician/{id}", dieticianId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("dietician_already_assigned", result2.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void limitExceededDieticianAssignTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "kgolonka",
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

        MvcResult result = mockMvc.perform(post("/api/mod/clients/assign-dietician/{id}", nineClientsDieticianId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("dietician_client_limit_exceeded", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    @Order(1)
    public void concurrentDieticianAssignmentTest() throws Exception {
        String user1LoginJson = """
            {
              "login": "mbiedr",
              "password": "P@ssw0rd!"
            }
            """;

        MvcResult loginResult1 = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1LoginJson))
                .andExpect(status().isOk())
                .andReturn();

        String token1 = objectMapper.readTree(loginResult1.getResponse().getContentAsString()).get("value").asText();

        String user2LoginJson = """
            {
              "login": "llornetka",
              "password": "P@ssw0rd!"
            }
            """;

        MvcResult loginResult2 = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2LoginJson))
                .andExpect(status().isOk())
                .andReturn();

        String token2 = objectMapper.readTree(loginResult2.getResponse().getContentAsString()).get("value").asText();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<MvcResult> future1 = executor.submit(() -> mockMvc.perform(post("/api/mod/clients/assign-dietician/{id}", nineClientsDieticianId)
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)).andReturn());

        Future<MvcResult> future2 = executor.submit(() -> mockMvc.perform(post("/api/mod/clients/assign-dietician/{id}", nineClientsDieticianId)
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)).andReturn());

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        MvcResult result1 = future1.get();
        MvcResult result2 = future2.get();

        int status1 = result1.getResponse().getStatus();
        int status2 = result2.getResponse().getStatus();

        Assertions.assertTrue((status1 == 200 && status2 != 200) || (status2 == 200 && status1 != 200));

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token1)).andReturn();
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token2)).andReturn();
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

        String random = UUID.randomUUID().toString();

        MvcResult result = mockMvc.perform(post("/api/mod/clients/assign-dietician/{id}", random)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn();

        Assertions.assertEquals("dietician_not_found", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void assignDieticianUnauthorizedTest() throws Exception {
        mockMvc.perform(post("/api/mod/clients/assign-dietician")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()).andReturn();
    }
}
