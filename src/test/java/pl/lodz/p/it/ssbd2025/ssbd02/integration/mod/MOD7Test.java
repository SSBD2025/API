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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.AsyncTestConfig;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestOrderDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.EmailService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Import(AsyncTestConfig.class)
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD7Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private EmailService emailService;

    @Test
    public void orderMedicalExaminationsTest() throws Exception  {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000013"),
                null,
                null,
                "Zlecenie badań",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        doNothing().when(emailService).sendBloodTestOrderNotificationEmail(anyString(), anyString(), any());

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsDieticianNotAssignedTest() throws Exception  {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000015"),
                null,
                null,
                "Zlecenie badań",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        MvcResult result = mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("client_has_no_assigned_dietician", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsDieticianAccessDeniedTest() throws Exception  {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000001338"),
                null,
                null,
                "Zlecenie badań",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        MvcResult result = mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()).andReturn();

        Assertions.assertEquals("dietician_access_denied", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsBloodTestAlreadyOrderedTest() throws Exception  {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("20000000-0000-0000-0000-000000000070"),
                null,
                null,
                "Zlecenie badań",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();

        MvcResult result = mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();

        Assertions.assertEquals("blood_test_already_ordered", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsClientNotFoundTest() throws Exception  {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000001111"),
                null,
                null,
                "Zlecenie badań",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        MvcResult result = mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn();

        Assertions.assertEquals("client_not_found", result.getResponse().getErrorMessage());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsUnauthorizedTest() throws Exception  {
        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000001111"),
                null,
                null,
                "Zlecenie badań",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    public void orderMedicalExaminationsNullDescriptionTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000006"),
                null,
                null,
                null,
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsDescriptionIsBlankTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000006"),
                null,
                null,
                "",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsDescriptionLessThen10CharactersTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000006"),
                null,
                null,
                "123456789",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsDescriptionMoreThen500CharactersTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000006"),
                null,
                null,
                """
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                        """,
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void orderMedicalExaminationsNoBloodParametersTest() throws Exception {
        String loginRequestJson = """
                {
                  "login": "tcheese",
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

        BloodTestOrderDTO bloodTestOrderDTO = new BloodTestOrderDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000006"),
                null,
                null,
                "to jest ok opis",
                List.of(),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }
}
