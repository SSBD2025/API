//package pl.lodz.p.it.ssbd2025.ssbd02.integration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
//import pl.lodz.p.it.ssbd2025.ssbd02.dto.AccountDTO;
//import pl.lodz.p.it.ssbd2025.ssbd02.dto.ClientDTO;
//import pl.lodz.p.it.ssbd2025.ssbd02.dto.UserRoleDTO;
//import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
//
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@Testcontainers
//public class testowyTest extends BaseIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    public void test() throws Exception {
//        UserRoleDTO.ClientDTO clientDTO = new UserRoleDTO.ClientDTO();
//
//        AccountDTO accountDTO = new AccountDTO(
//                null,
//                null,
//                "testuser",
//                "P@ssw0rd!",
//                null,
//                null,
//                "Joe",
//                "Doe",
//                "joe.doe@gmail.com",
//                null,
//                null,
//                Language.pl_PL,
//                null,
//                null
//        );
//
//        ClientDTO clientDTO2 = new ClientDTO(clientDTO, accountDTO);
//
//        String requestJson = objectMapper.writeValueAsString(clientDTO2);
//
//        mockMvc.perform(post("/api/client/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestJson))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.account.login").value("testuser"))
//                .andExpect(jsonPath("$.account.firstName").value("Joe"));
//    }
//}
