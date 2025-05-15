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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import java.util.UUID;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOK6 extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AccountTestHelper accountTestHelper;

    @MockitoBean
    private JavaMailSender mailSender;

    String adminToken;

    @BeforeEach
    void setup() throws Exception {
        // Logowanie jako admin
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
        adminToken = objectMapper.readTree(adminResponseJson).get("accessToken").asText();
    }


    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + adminToken)).andReturn();
    }

    // POSITIVE TESTS //
    // POSITIVE TESTS //
    // POSITIVE TESTS //

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
        Assertions.assertTrue(response.contains("accessToken"));
        JSONObject json = new JSONObject(response);
        String agorgonzolaToken = json.getString("accessToken");
        Assertions.assertTrue(tokenProvider.getRoles(agorgonzolaToken).contains("ADMIN"));

        mockMvc.perform(post("/api/account/logout")
                        .header("Authorization", "Bearer " + agorgonzolaToken))
                .andExpect(status().isOk());
    }


    @Test
    public void assignDieticianRoleTest() throws Exception {
        UUID id = accountTestHelper.getClientByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/dietician")
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
        Assertions.assertTrue(response.contains("accessToken"));
        JSONObject json = new JSONObject(response);
        String agorgonzolaToken = json.getString("accessToken");
        Assertions.assertTrue(tokenProvider.getRoles(agorgonzolaToken).contains("DIETICIAN"));

        mockMvc.perform(post("/api/account/logout")
                        .header("Authorization", "Bearer " + agorgonzolaToken))
                .andExpect(status().isOk());
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
        String token = json.getString("accessToken");
        Assertions.assertTrue(tokenProvider.getRoles(token).contains("CLIENT"));

        mockMvc.perform(post("/api/account/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
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
        String agorgonzolaToken = json.getString("accessToken");
        UUID id = accountTestHelper.getClientByLogin("agorgonzola").getId();

        mockMvc.perform(put("/api/account/" + id + "/roles/admin")
                        .header("Authorization", "Bearer " + agorgonzolaToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/account/logout")
                        .header("Authorization", "Bearer " + agorgonzolaToken))
                .andExpect(status().isOk());
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

        mockMvc.perform(put("/api/account/" + id + "/roles/dietician")
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
        String token = json.getString("accessToken");
        var roles = tokenProvider.getRoles(token);
        Assertions.assertTrue(roles.contains("ADMIN"));
        Assertions.assertTrue(roles.contains("DIETICIAN"));

        mockMvc.perform(post("/api/account/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
