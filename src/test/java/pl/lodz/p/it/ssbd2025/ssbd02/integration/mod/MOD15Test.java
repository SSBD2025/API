package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FoodPyramidTestHelper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOD15Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String dieticianToken;
    private String clientToken;

    private final String dieticianLogin = "tcheese";
    private final String clientLogin = "agorgonzola";

    private String loginAndGetToken(String login, String password) throws Exception {
        String loginJson = """
                {
                  "login": "%s",
                  "password": "%s"
                }
                """.formatted(login, password);

        MvcResult result = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("value").asText();
    }

    @BeforeEach
    public void setup() throws Exception {
        dieticianToken = loginAndGetToken(dieticianLogin, "P@ssw0rd!");
        clientToken = loginAndGetToken(clientLogin, "P@ssw0rd!");
    }

    @AfterEach
    public void teardown() throws Exception {
        if (dieticianToken != null) {
            mockMvc.perform(post("/api/account/logout")
                    .header("Authorization", "Bearer " + dieticianToken));
        }
        if (clientToken != null) {
            mockMvc.perform(post("/api/account/logout")
                    .header("Authorization", "Bearer " + clientToken));
        }
    }

    @Test
    public void testGetAllFoodPyramids_AsDietician_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/mod/food-pyramids")
                        .header("Authorization", "Bearer " + dieticianToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").isString());
    }

    @Test
    public void testGetAllFoodPyramids_AsClient_ShouldFailWithForbidden() throws Exception {
        mockMvc.perform(get("/api/mod/food-pyramids")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
