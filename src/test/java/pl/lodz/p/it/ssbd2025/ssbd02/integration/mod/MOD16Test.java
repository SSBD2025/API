package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.hamcrest.Matchers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.BloodTestOrderDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.BloodParameter;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


//Przegladanie listy niewypelnionych zleconych badan
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")

public class MOD16Test extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setup() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void getUnfulfilledBloodTestOrdersTest() throws Exception {
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
                "Zlecenie badan",
                List.of(BloodParameter.GLUCOSE, BloodParameter.BASO),
                false
        );

        BloodTestOrderDTO bloodTestOrderDTO2 = new BloodTestOrderDTO(
                UUID.fromString("10000000-0000-0000-0000-000000000070"),
                null,
                null,
                "Zlecenie badan1",
                List.of(BloodParameter.B6, BloodParameter.CA, BloodParameter.CHOL),
                false
        );

        String bloodTestOrder = objectMapper.writeValueAsString(bloodTestOrderDTO);
        String bloodTestOrder2 = objectMapper.writeValueAsString(bloodTestOrderDTO2);

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/mod/dieticians/order-medical-examinations")
                        .header("Authorization", "Bearer " + token)
                        .content(bloodTestOrder2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/mod/dieticians/orders")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))

                .andExpect(jsonPath("$[0].bloodTestOrderDTO.clientId").value("00000000-0000-0000-0000-000000000006"))
                .andExpect(jsonPath("$[0].bloodTestOrderDTO.description").value("Very descriptive description"))
                .andExpect(jsonPath("$[0].bloodTestOrderDTO.parameters.length()").value(0))
                .andExpect(jsonPath("$[0].minimalClientDTO.id").value("00000000-0000-0000-0000-000000000006"))
                .andExpect(jsonPath("$[0].minimalClientDTO.firstName").value("Anthony"))

                .andExpect(jsonPath("$[1].bloodTestOrderDTO.clientId").value("00000000-0000-0000-0000-000000000013"))
                .andExpect(jsonPath("$[1].bloodTestOrderDTO.description").value("Zlecenie badan"))
                .andExpect(jsonPath("$[1].bloodTestOrderDTO.parameters.length()").value(2))
                .andExpect(jsonPath("$[1].bloodTestOrderDTO.parameters", hasItems("GLUCOSE", "BASO")))
                .andExpect(jsonPath("$[1].minimalClientDTO.id").value("00000000-0000-0000-0000-000000000013"))
                .andExpect(jsonPath("$[1].minimalClientDTO.firstName").value("Krzysztof"))


                .andExpect(jsonPath("$[2].bloodTestOrderDTO.clientId").value("10000000-0000-0000-0000-000000000070"))
                .andExpect(jsonPath("$[2].bloodTestOrderDTO.description").value("Zlecenie badan1"))
                .andExpect(jsonPath("$[2].bloodTestOrderDTO.parameters.length()").value(3))
                .andExpect(jsonPath("$[2].bloodTestOrderDTO.parameters", hasItems("B6", "CA", "CHOL")))
                .andExpect(jsonPath("$[2].minimalClientDTO.id").value("10000000-0000-0000-0000-000000000070"))
                .andExpect(jsonPath("$[2].minimalClientDTO.firstName").value("James"));

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();

    }

    @Test
    public void getUnfulfilledBloodTestOrdersNoTokenProvidedTest() throws Exception {
        mockMvc.perform(get("/api/mod/dieticians/orders")).andExpect(status().isUnauthorized());
    }

    @Test
    public void getUnfulfilledBloodTestOrdersInvalidTokenProvidedTest() throws Exception {
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

        mockMvc.perform(get("/api/mod/dieticians/orders")
                        .header("Authorization", "Bearer " + token + "1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();

    }

    @Test
    public void getUnfulfilledBloodTestOrdersAsClientTest() throws Exception {
        String loginClientRequestJson = """
        {
          "login": "agorgonzola",
          "password": "P@ssw0rd!"
        }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginClientRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(get("/api/mod/dieticians/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();

    }

    @Test
    public void getUnfulfilledBloodTestOrdersAsAdminTest() throws Exception {
        String loginAdminRequestJson = """
        {
          "login": "jcheddar",
          "password": "P@ssw0rd!"
        }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginAdminRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(get("/api/mod/dieticians/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();

    }
}
