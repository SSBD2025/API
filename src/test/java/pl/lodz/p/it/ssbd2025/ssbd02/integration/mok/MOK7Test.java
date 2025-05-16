package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK7Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AccountTestHelper accountTestHelper;

    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        String loginJson = """
        {
          "login": "jcheddar",
          "password": "P@ssw0rd!"
        }
        """;

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(response).get("value").asText();
    }

    @AfterEach
    void cleanup() throws Exception {
        if (adminToken != null) {
            mockMvc.perform(post("/api/account/logout")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());
            adminToken = null;
        }
    }

    // ============ POSITIVE TESTS ============

    @Test
    public void unassignAdminRoleTest() throws Exception {
        UUID id = accountTestHelper.getClientWithRolesByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/account/" + id + "/roles/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void unassignDieticianRoleTest() throws Exception {
        UUID id = accountTestHelper.getClientWithRolesByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/dietician")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/account/" + id + "/roles/dietician")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    public void unassignClientRoleTest() throws Exception {
        UUID id = accountTestHelper.getClientWithRolesByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/client")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/account/" + id + "/roles/client")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    // ============ NEGATIVE TESTS ============

    @Test
    public void unassignRoleFromNonExistentUser_ShouldReturn404() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(delete("/api/account/" + fakeId + "/roles/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void unassignNotAssignedRole_ShouldReturn404() throws Exception {
        UUID id = accountTestHelper.getClientWithRolesByLogin("agorgonzola").getId();

        mockMvc.perform(delete("/api/account/" + id + "/roles/dietician")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void unassignInvalidRole_ShouldReturn404() throws Exception {
        UUID id = accountTestHelper.getClientWithRolesByLogin("agorgonzola").getId();

        mockMvc.perform(delete("/api/account/" + id + "/roles/superuser")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
