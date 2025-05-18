package pl.lodz.p.it.ssbd2025.ssbd02.integration.mok;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MOK4Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private AccountTestHelper accountTestHelper;

    String adminToken;

    UUID adminId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID dieticianId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    UUID clinetId = UUID.fromString("00000000-0000-0000-0000-000000000005");
    UUID adminDieticianId = UUID.fromString("00000000-0000-0000-0000-000000000007");

    @BeforeEach
    void setup() throws Exception {
        String loginRequestJson = """
        {
          "login": "jcheddar",
          "password": "P@ssw0rd!"
        }
        """;

        doNothing().when(emailService).sendAdminLoginEmail(anyString(), anyString(), anyString(), any());

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(responseJson).get("value").asText();

    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + adminToken)).andReturn();
    }

    @Test
    public void blockUserPositiveTest() throws Exception {
        doNothing().when(emailService).sendBlockAccountEmail(anyString(), anyString(), any());

        mockMvc.perform(post("/api/account/" + dieticianId.toString() +"/block")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Assertions.assertFalse(accountTestHelper.getClientByLogin("tcheese").isActive());
    }

    @Test
    public void selfBlockNegativeTest() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/account/" + adminId.toString() +"/block")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andReturn();

        Assertions.assertEquals("self_block", result.getResponse().getErrorMessage());
        Assertions.assertTrue(accountTestHelper.getClientByLogin("jcheddar").isActive());
    }

    @Test
    public void blockAlreadyBlockedUserNegativeTest() throws Exception {
        mockMvc.perform(post("/api/account/" + adminDieticianId.toString() +"/block")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        Assertions.assertFalse(accountTestHelper.getClientByLogin("drice").isActive());

        MvcResult result = mockMvc.perform(post("/api/account/" + adminDieticianId.toString() +"/block")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andReturn();

        Assertions.assertEquals("account_already_blocked", result.getResponse().getErrorMessage());
        Assertions.assertFalse(accountTestHelper.getClientByLogin("drice").isActive());
    }

    @Test
    public void blockAccountBadRequestNegativeTest() throws Exception {
        mockMvc.perform(post("/api/account/000/block")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void blockAccountNotFoundNegativeTest() throws Exception {
        MvcResult result =  mockMvc.perform(post("/api/account/00000000-0000-0000-0000-000000000111/block")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andReturn();

        Assertions.assertEquals("account_not_found", result.getResponse().getErrorMessage());

    }

    @Test
    public void blockAccountNoTokenProvidedTest() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/account/" + dieticianId.toString() +"/block"))
                .andExpect(status().isForbidden())
                .andReturn();

        Assertions.assertEquals("Access Denied", result.getResponse().getErrorMessage());

    }

    @Test
    public void blockAccountInvalidTokenTest() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/account/" + adminId.toString() +"/block")
                        .header("Authorization", "Bearer " + adminToken + "1"))
                .andExpect(status().isForbidden())
                .andReturn();

        Assertions.assertEquals("Access Denied", result.getResponse().getErrorMessage());
        Assertions.assertTrue(accountTestHelper.getClientByLogin("jcheddar").isActive());
    }

    @Test
    public void blockAccountNoPrevillageNegativeTest() throws Exception {
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
        String clientToken = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(post("/api/account/" + adminId.toString() +"/block")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
        Assertions.assertTrue(accountTestHelper.getClientByLogin("jcheddar").isActive());
    }
}
