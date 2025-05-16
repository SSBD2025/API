package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Account;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK6Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AccountTestHelper accountTestHelper;

    private String adminToken;
    private String agorgonzolaToken = null;

    @BeforeEach
    void setup() throws Exception {
        String adminLoginJson = """
        {
          "login": "jcheddar",
          "password": "P@ssw0rd!"
        }
        """;

        MvcResult adminLoginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminLoginJson))
                .andExpect(status().isOk())
                .andReturn();

        String adminResponseJson = adminLoginResult.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(adminResponseJson).get("value").asText();
    }

    @AfterEach
    void teardown() throws Exception {
        if (adminToken != null) {
            mockMvc.perform(post("/api/account/logout")
                            .header("Authorization", "Bearer " + adminToken))
                    .andReturn();
        }

        if (agorgonzolaToken != null) {
            mockMvc.perform(post("/api/account/logout")
                            .header("Authorization", "Bearer " + agorgonzolaToken))
                    .andExpect(status().isOk());
            agorgonzolaToken = null;
        }
    }

    @Test
    public void assignAdminRoleTest() throws Exception {
        UUID id = accountTestHelper.getClientByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "login": "agorgonzola",
                          "password": "P@ssw0rd!"
                        }
                        """))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(response);
        agorgonzolaToken = json.getString("value");

        Assertions.assertTrue(tokenProvider.getRoles(agorgonzolaToken).contains("ADMIN"));

        Account updated = accountTestHelper.getClientWithRolesByLogin("agorgonzola");
        boolean hasAdmin = updated.getUserRoles().stream()
                .anyMatch(role -> role.getRoleName().equals("ADMIN") && role.isActive());

        Assertions.assertTrue(hasAdmin, "Account in DB should have active ADMIN role");
    }

    @Test
    public void assignDieticianRoletoClientTestShouldReturn409() throws Exception {
        UUID id = accountTestHelper.getClientByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/dietician")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "login": "agorgonzola",
                              "password": "P@ssw0rd!"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(response);
        agorgonzolaToken = json.getString("value");

        Assertions.assertFalse(tokenProvider.getRoles(agorgonzolaToken).contains("DIETICIAN"));

        Account updated = accountTestHelper.getClientWithRolesByLogin("agorgonzola");
        boolean hasDietician = updated.getUserRoles().stream()
                .anyMatch(role -> role.getRoleName().equals("DIETICIAN") && role.isActive());

        Assertions.assertFalse(hasDietician, "Account in DB should not have active DIETICIAN role");
    }

    @Test
    public void assignClientRoleTest() throws Exception {
        UUID id = accountTestHelper.getClientByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/client")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "login": "agorgonzola",
                              "password": "P@ssw0rd!"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(response);
        agorgonzolaToken = json.getString("value");

        Assertions.assertTrue(tokenProvider.getRoles(agorgonzolaToken).contains("CLIENT"));

        Account updated = accountTestHelper.getClientWithRolesByLogin("agorgonzola");
        boolean hasClient = updated.getUserRoles().stream()
                .anyMatch(role -> role.getRoleName().equals("CLIENT") && role.isActive());

        Assertions.assertTrue(hasClient, "Account in DB should have active CLIENT role");
    }

    @Test
    public void assignRoleToNonExistentUserShouldReturn404() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(put("/api/account/" + fakeId + "/roles/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void userCannotAssignRoleToSelf_ShouldReturn403() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "login": "agorgonzola",
                              "password": "P@ssw0rd!"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(response);
        agorgonzolaToken = json.getString("value");

        UUID id = accountTestHelper.getClientByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/admin")
                        .header("Authorization", "Bearer " + agorgonzolaToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void adminCannotAssignInvalidRole_ShouldReturn404() throws Exception {
        UUID id = accountTestHelper.getClientByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/superuser")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void assignMultipleRolesTest() throws Exception {
        UUID id = accountTestHelper.getClientByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/account/" + id + "/roles/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "login": "agorgonzola",
                              "password": "P@ssw0rd!"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(response);
        agorgonzolaToken = json.getString("value");

        var roles = tokenProvider.getRoles(agorgonzolaToken);
        Assertions.assertTrue(roles.contains("ADMIN"));

        Account updated = accountTestHelper.getClientWithRolesByLogin("agorgonzola");
        boolean hasAdmin = updated.getUserRoles().stream()
                .anyMatch(role -> role.getRoleName().equals("ADMIN") && role.isActive());

        Assertions.assertTrue(hasAdmin, "Account in DB should have active ADMIN role");
    }
}
