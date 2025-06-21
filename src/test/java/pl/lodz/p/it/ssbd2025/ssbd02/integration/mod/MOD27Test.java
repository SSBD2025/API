package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.FeedbackDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.Feedback;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.FoodPyramid;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FeedbackTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.FoodPyramidTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


//Edycja opinii
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "modTransactionManager")
public class MOD27Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LockTokenService lockTokenService;

    @Autowired
    private FeedbackTestHelper feedbackTestHelper;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private FoodPyramidTestHelper foodPyramidTestHelper;

    private final String feedbackId = "00000000-0000-0000-0000-000000000021";
    private final String pyramidId = "00000000-0000-0000-0000-000000000011";

    @BeforeEach
    void setup() throws Exception {
        MimeMessage realMimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

        @Test
    public void updateFeedbackTest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        FoodPyramid foodPyramid1 = foodPyramidTestHelper.getFoodPyramid(UUID.fromString(pyramidId));
        Feedback feedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(1);
        feedbackDTO.setDescription("Nie pomaga");
        feedbackDTO.setLockToken(lockToken);
        SecurityContextHolder.clearContext();

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
        String token = objectMapper.readTree(responseJson).get("value").asText();

       mockMvc.perform(put("/api/mod/feedbacks")
            .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(feedbackDTO))
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Nie pomaga"))
            .andExpect(jsonPath("$.rating").value(1));


        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        FoodPyramid foodPyramid2 = foodPyramidTestHelper.getFoodPyramid(UUID.fromString(pyramidId));
        Feedback updatedFeedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));

        Assertions.assertNotNull(updatedFeedback);
        Assertions.assertEquals(updatedFeedback.getRating(), feedbackDTO.getRating());
        Assertions.assertEquals(updatedFeedback.getDescription(), feedbackDTO.getDescription());
        Assertions.assertNotEquals(foodPyramid1.getAverageRating(), foodPyramid2.getAverageRating());


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();

        SecurityContextHolder.clearContext();
    }

    @Test
    public void updateNotYourFeedbackFeedbackTest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("kkaktus", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        Feedback feedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(3);
        feedbackDTO.setDescription("W porzadku");
        feedbackDTO.setLockToken(lockToken);
        SecurityContextHolder.clearContext();

        String loginRequestJson = """
        {
          "login": "kkaktus",
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



        MvcResult result = mockMvc.perform(put("/api/mod/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andReturn();

        Assertions.assertEquals("not_your_feedback", result.getResponse().getErrorMessage());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("kkaktus", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        Feedback updatedFeedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));

        Assertions.assertNotNull(updatedFeedback);
        Assertions.assertNotEquals(updatedFeedback.getRating(), feedbackDTO.getRating());
        Assertions.assertNotEquals(updatedFeedback.getDescription(), feedbackDTO.getDescription());


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();

        SecurityContextHolder.clearContext();
    }

    @Test
    public void updateFeedbackInvalidLockTokenTest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        Feedback feedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();
        lockToken = lockToken.concat("1");

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(2);
        feedbackDTO.setDescription("Nie jestem zadowolony");
        feedbackDTO.setLockToken(lockToken);
        SecurityContextHolder.clearContext();

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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(put("/api/mod/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );

        Feedback updatedFeedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));

        Assertions.assertNotNull(updatedFeedback);
        Assertions.assertNotEquals(updatedFeedback.getRating(), feedbackDTO.getRating());
        Assertions.assertNotEquals(updatedFeedback.getDescription(), feedbackDTO.getDescription());

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();

        SecurityContextHolder.clearContext();
    }

    @Test
    public void updateFeedbackTestNoTokenProvided() throws Exception {
        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(1);
        feedbackDTO.setDescription("Nie pomaga");

        String body = objectMapper.writeValueAsString(feedbackDTO);

        mockMvc.perform(put("/api/mod/feedbacks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateFeedbackTestInvalidTokenProvidedTest() throws Exception {
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

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(1);
        feedbackDTO.setDescription("Nie pomaga");
        feedbackDTO.setLockToken("1");

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();
        String body = objectMapper.writeValueAsString(feedbackDTO);

        mockMvc.perform(put("/api/mod/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + token + "1"))
                .andExpect(status().isUnauthorized());


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void updateFeedbackTestAsDieticianTest() throws Exception {
        String loginDieticianRequestJson = """
        {
          "login": "tcheese",
          "password": "P@ssw0rd!"
        }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDieticianRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(1);
        feedbackDTO.setDescription("Nie pomaga");
        feedbackDTO.setLockToken("lt");

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();
        String body = objectMapper.writeValueAsString(feedbackDTO);

        mockMvc.perform(put("/api/mod/feedbacks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void updateFeedbackTestAsAdminTest() throws Exception {
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

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(1);
        feedbackDTO.setDescription("Nie pomaga");
        feedbackDTO.setLockToken("lt");

        String responseJson = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("value").asText();
        String body = objectMapper.writeValueAsString(feedbackDTO);

        mockMvc.perform(put("/api/mod/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void  updateFeedbackRatingTooLowTest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );
        Feedback feedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(-1);
        feedbackDTO.setDescription("Nie pomaga");
        feedbackDTO.setLockToken(lockToken);
        SecurityContextHolder.clearContext();

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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(put("/api/mod/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("rating"))
                .andExpect(jsonPath("$.violations[0].message").value("must be greater than or equal to 1"));


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void  updateFeedbackRatingTooHighTest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );
        Feedback feedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(6);
        feedbackDTO.setDescription("Nie pomaga");
        feedbackDTO.setLockToken(lockToken);
        SecurityContextHolder.clearContext();

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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(put("/api/mod/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("rating"))
                .andExpect(jsonPath("$.violations[0].message").value("must be less than or equal to 5"));


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void  updateFeedbackDescriptionTooShortTest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );
        Feedback feedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(3);
        feedbackDTO.setDescription("");
        feedbackDTO.setLockToken(lockToken);
        SecurityContextHolder.clearContext();

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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(put("/api/mod/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(2));

        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

    @Test
    public void  updateFeedbackDescriptionTooLongTest() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")))
        );
        Feedback feedback = feedbackTestHelper.getFeedback(UUID.fromString(feedbackId));
        String lockToken = lockTokenService.generateToken(feedback.getId(), feedback.getVersion()).getValue();

        FeedbackDTO feedbackDTO = new FeedbackDTO();
        feedbackDTO.setRating(3);
        feedbackDTO.setDescription("slnvjsdkavnsdjkvnsdjkvnsdkjvnasdjkvnsdjklvnsadjklvnsdkjlvnsdajkvsdnklvjasdnvjkasdnvkjlsd" +
                "slnvjsdkavnsdjkvnsdjkvnsdkjvnasdjkvnsdjklvnsadjklvnsdkjlvnsdajkvsdnklvjasdnvjkasdnvkjlsd" +
                "slnvjsdkavnsdjkvnsdjkvnsdkjvnasdjkvnsdjklvnsadjklvnsdkjlvnsdajkvsdnklvjasdnvjkasdnvkjlsd" +
                "slnvjsdkavnsdjkvnsdjkvnsdkjvnasdjkvnsdjklvnsadjklvnsdkjlvnsdajkvsdnklvjasdnvjkasdnvkjlsd");
        feedbackDTO.setLockToken(lockToken);
        SecurityContextHolder.clearContext();

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
        String token = objectMapper.readTree(responseJson).get("value").asText();

        mockMvc.perform(put("/api/mod/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("description"))
                .andExpect(jsonPath("$.violations[0].message").value("length must be between 1 and 255"));


        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }

}
