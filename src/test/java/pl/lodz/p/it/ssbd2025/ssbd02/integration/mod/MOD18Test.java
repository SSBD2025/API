package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.AssignDietPlanDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientFoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.AccountTestHelper;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FoodPyramidTestHelper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOD18Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FoodPyramidTestHelper foodPyramidTestHelper;

    private UUID clientId = UUID.fromString("20000000-0000-0000-0000-000000000070");
    private UUID foodPyramidId;
    private String dieticianToken;

    private final String dieticianLogin = "tcheese";

    private static final String ENDPOINT = "/api/mod/client-food-pyramids";

    private String loginAndGetToken(String login, String password) throws Exception {
        String loginJson = """
            {
              "login": "%s",
              "password": "%s"
            }
            """.formatted(login, password);

        return objectMapper.readTree(
                        mockMvc.perform(post("/api/account/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(loginJson))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString())
                .get("value").asText();
    }

    @BeforeEach
    public void setup() throws Exception {
        dieticianToken = loginAndGetToken(dieticianLogin, "P@ssw0rd!");
        foodPyramidId = foodPyramidTestHelper.getExistingFoodPyramid().getId();
    }

    @AfterEach
    public void teardown() throws Exception {
        if (dieticianToken != null) {
            mockMvc.perform(post("/api/account/logout")
                    .header("Authorization", "Bearer " + dieticianToken));
        }
    }

    @Test
    void shouldAssignFoodPyramidSuccessfully() throws Exception {
        AssignDietPlanDTO dto = new AssignDietPlanDTO();
        dto.setClientId(clientId);
        dto.setFoodPyramidId(foodPyramidId);
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + dieticianToken)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
        TestingAuthenticationToken auth =
                new TestingAuthenticationToken("tcheese", "P@ssw0rd!", "ROLE_DIETICIAN");
        SecurityContextHolder.getContext().setAuthentication(auth);
        List<ClientFoodPyramid> assignments = foodPyramidTestHelper.getClientFoodPyramids(clientId);
        assertThat(assignments).hasSize(1);
        assertThat(assignments.get(0).getFoodPyramid().getId()).isEqualTo(foodPyramidId);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldFailWhenAssigningAlreadyAssignedPyramid() throws Exception {
        AssignDietPlanDTO dto2 = new AssignDietPlanDTO();
        dto2.setClientId(clientId);
        dto2.setFoodPyramidId(foodPyramidId);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + dieticianToken)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailWhenClientDoesNotExist() throws Exception {
        AssignDietPlanDTO dto = new AssignDietPlanDTO();
        dto.setClientId(UUID.randomUUID());
        dto.setFoodPyramidId(foodPyramidId);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + dieticianToken)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailWhenFoodPyramidDoesNotExist() throws Exception {
        AssignDietPlanDTO dto = new AssignDietPlanDTO();
        dto.setClientId(clientId);
        dto.setFoodPyramidId(UUID.randomUUID());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + dieticianToken)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
