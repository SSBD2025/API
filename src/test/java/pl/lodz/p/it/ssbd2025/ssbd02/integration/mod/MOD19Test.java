package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
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
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FoodPyramidDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FoodPyramidTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOD19Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private FoodPyramidTestHelper foodPyramidTestHelper;

    private static final UUID EXISTING_CLIENT_ID = UUID.fromString("40000000-0000-0000-0000-000000000070");

    private FoodPyramidDTO buildValidFoodPyramidDTO(String name) {
        FoodPyramidDTO dto = new FoodPyramidDTO();
        dto.setName(name);
        dto.setKcal(2000);
        dto.setFat(70);
        dto.setSaturatedFattyAcids(20);
        dto.setCarbohydrates(300);
        dto.setSugar(100);
        dto.setProtein(50);

        dto.setA(700);
        dto.setD(10);
        dto.setE(12);
        dto.setK(90);
        dto.setB1(1.2);
        dto.setB2(1.3);
        dto.setB3(16);
        dto.setB5(5);
        dto.setB6(1.3);
        dto.setB7(30);
        dto.setB9(400);
        dto.setB12(2.4);
        dto.setC(90);
        dto.setPotassium(3500);
        dto.setCalcium(1000);
        dto.setPhosphorus(700);
        dto.setMagnesium(400);
        dto.setIron(18);
        dto.setZinc(11);
        dto.setFluorine(3);
        dto.setManganese(2.3);
        dto.setCopper(0.9);
        dto.setIodine(150);
        dto.setSelenium(55);
        dto.setMolybdenum(45);
        dto.setChromium(35);

        return dto;
    }

    private String token;
    @BeforeEach
    void setup() throws Exception {
        String loginRequestJson = """
                {
                  "login": "drice",
                  "password": "P@ssw0rd!"
                }
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson))
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
    void createAndAssign_Success() throws Exception {
        FoodPyramidDTO dto = buildValidFoodPyramidDTO("NewCreatedTest");
        dto.setFat(69);

        mockMvc.perform(post("/api/mod/client-food-pyramids/new/" + EXISTING_CLIENT_ID)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk());

        Assertions.assertEquals("FoodPyramid", foodPyramidTestHelper.findByName("NewCreatedTest").getClass().getSimpleName());
        foodPyramidTestHelper.deleteByName("NewCreatedTest");
    }

    @Test
    void createAndAssign_PyramidAlreadyAssigned() throws Exception {
        String pyramidName = "TwiceAssigned";
        FoodPyramidDTO dto = buildValidFoodPyramidDTO(pyramidName);
        dto.setFat(68);

        mockMvc.perform(post("/api/mod/client-food-pyramids/new/" + EXISTING_CLIENT_ID)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/mod/client-food-pyramids/new/" + EXISTING_CLIENT_ID)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());

        Assertions.assertEquals("FoodPyramid", foodPyramidTestHelper.findByName(pyramidName).getClass().getSimpleName());
        foodPyramidTestHelper.deleteByName(pyramidName);
    }

    @Test
    void assignExisting_Success() throws Exception {

        String foodPyramidPayload = """
                {
                     "id": "00000000-0000-0000-0000-000000000099",
                     "averageRating": 0.0,
                     "name": "K40-Test",
                     "kcal": 1800,
                     "fat": 70.0,
                     "saturatedFattyAcids": 20.0,
                     "carbohydrates": 260.0,
                     "sugar": 90.0,
                     "protein": 50.0,
                     "a": 0.8,
                     "d": 0.005,
                     "e": 12.0,
                     "k": 0.075,
                     "b1": 1.1,
                     "b2": 1.4,
                     "b3": 16.0,
                     "b5": 6.0,
                     "b6": 1.4,
                     "b7": 0.05,
                     "b9": 0.2,
                     "b12": 0.0025,
                     "c": 80.0,
                     "potassium": 2000.0,
                     "calcium": 1000.0,
                     "phosphorus": 700.0,
                     "magnesium": 375.0,
                     "iron": 14.0,
                     "zinc": 10.0,
                     "fluorine": 3.5,
                     "manganese": 2.0,
                     "copper": 1.0,
                     "iodine": 0.15,
                     "selenium": 0.055,
                     "molybdenum": 0.05,
                     "chromium": 0.04
                 }
                """;

        mockMvc.perform(post("/api/mod/client-food-pyramids/new/" + EXISTING_CLIENT_ID)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(foodPyramidPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id.foodPyramidId").value("00000000-0000-0000-0000-000000000013"));
    }

    @Test
        void createAndAssign_ForbiddenForClient() throws Exception {
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
        String pyramidName = "Forbidden";
        FoodPyramidDTO dto = buildValidFoodPyramidDTO(pyramidName);
        dto.setFat(67);

        mockMvc.perform(post("/api/mod/client-food-pyramids/new/" + EXISTING_CLIENT_ID)
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        Assertions.assertThrows(IllegalArgumentException.class, () -> foodPyramidTestHelper.findByName(pyramidName));
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken)).andReturn();
    }

    @Test
    @WithMockUser(roles = "DIETICIAN")
    void createAndAssign_ClientNotExist() throws Exception {
        UUID nonExistentClientId = UUID.randomUUID();
        String pyramidName = "NonExistentClient";
        FoodPyramidDTO dto = buildValidFoodPyramidDTO(pyramidName);
        dto.setFat(66);

        mockMvc.perform(post("/api/mod/client-food-pyramids/new/" + nonExistentClientId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
        Assertions.assertThrows(IllegalArgumentException.class, () -> foodPyramidTestHelper.findByName(pyramidName));
    }
}
