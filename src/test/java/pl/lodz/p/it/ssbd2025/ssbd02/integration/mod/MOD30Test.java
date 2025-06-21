package pl.lodz.p.it.ssbd2025.ssbd02.integration.mod;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.ssbd2025.ssbd02.config.BaseIntegrationTest;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.LoginDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.BloodTestResult;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.ClientBloodTestReport;
import pl.lodz.p.it.ssbd2025.ssbd02.helpers.ClientBloodTestReportTestHelper;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.LockTokenService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class MOD30Test extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientBloodTestReportTestHelper helper;

    @Autowired
    private LockTokenService lockTokenService;

    private String token;
    UUID reportId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setup() throws Exception {
        LoginDTO loginDTO = new LoginDTO("drice", "P@ssw0rd!");
        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = loginResult.getResponse().getContentAsString();
        token = objectMapper.readTree(responseJson).get("value").asText();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken("drice", "P@ssw0rd!", List.of(new SimpleGrantedAuthority("ROLE_DIETICIAN")));
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void teardown() throws Exception {
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + token)).andReturn();
    }


    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void edit_SUCCESS_test() throws Exception {
//        SecurityContext context = SecurityContextHolder.createEmptyContext();
//        Authentication auth = new UsernamePasswordAuthenticationToken("drice", "P@ssw0rd!", List.of(new SimpleGrantedAuthority("ROLE_DIETICIAN")));
//        context.setAuthentication(auth);
//        SecurityContextHolder.setContext(context);
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();
        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(255, pltAfter.getResult(), "PLT should be updated to 255");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void edit_bloodTestResultVersion_CONFLICT_test() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String clientBloodTestReportToken = lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue();
        String bloodTestResultToken = lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": "%s",
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                clientBloodTestReportToken,
                bloodTestResultToken,
                275
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(275, pltAfter.getResult(), "PLT should be updated to 255");
        String payload2 = payload.formatted(
                clientBloodTestReportToken,
                bloodTestResultToken,
                260
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload2)
                )
                .andExpect(status().isConflict());

        var updated = helper.getClientBloodTestReportById(reportId);
        var updatedResultOpt = updated.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst();

        assertTrue(updatedResultOpt.isPresent(), "Updated PLT result should exist");
        assertEquals(275, updatedResultOpt.get().getResult());
    }


//    @Test
//    @WithMockUser(roles = {"DIETICIAN"})
//    void edit_invalidToken_Test() throws Exception {
//        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
//        BloodTestResult pltBefore = beforeReport.getResults().stream()
//                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
//                .findFirst()
//                .orElseThrow();
//
//        String bloodTestResultToken = lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue();
//
//        String payload = """
//        {
//            "lockToken": "%s",
//            "results": [
//                {
//                    "id": null,
//                    "lockToken": "%s",
//                    "result": "%s",
//                    "bloodParameter": {
//                        "name": "PLT",
//                        "description": "Platelets",
//                        "unit": "X10_6_U_L",
//                        "standardMin": 150.0,
//                        "standardMax": 450.0
//                    }
//                }
//            ]
//        }""".formatted(
//                "MTExMTExMTEtMTExMS0xMTExLTExMTEtMTExMTExMTExMTExOjA6RUxlVjBjajk2SnZzNTlvMGkzTWdPZkVveUN6K0hVbHFwbEEvMDJhWjRsYz0=",
//                bloodTestResultToken,
//                275
//        );
//        mockMvc.perform(put("/api/mod/blood-test-reports")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(payload))
//                .andExpect(status().isNotFound());
//    }
    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void edit_clientBloodTestReportIdNotFound_Test() throws Exception {
//        SecurityContext context = SecurityContextHolder.createEmptyContext();
//        Authentication auth = new UsernamePasswordAuthenticationToken("drice", "P@ssw0rd!", List.of(new SimpleGrantedAuthority("ROLE_DIETICIAN")));
//        context.setAuthentication(auth);
//        SecurityContextHolder.setContext(context);
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();
        String clientBloodTestReportToken = lockTokenService.generateToken(pltBefore.getId(), beforeReport.getVersion()).getValue();
        String bloodTestResultToken = lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": "%s",
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                clientBloodTestReportToken,
                bloodTestResultToken,
                275
        );
        MvcResult result = mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound()).andReturn();
        String errorMessage = result.getResponse().getErrorMessage();
        Assertions.assertEquals("client_blood_test_report_not_found", errorMessage);

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltAfter.getResult(), pltBefore.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void edit_bloodTestResultIdNotFound_Test() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();


        String clientBloodTestReportToken = lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue();
        String bloodTestResultToken = lockTokenService.generateToken(reportId, pltBefore.getVersion()).getValue();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": "%s",
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                clientBloodTestReportToken,
                bloodTestResultToken,
                275
        );
        MvcResult result = mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound()).andReturn();
        String errorMessage = result.getResponse().getErrorMessage();
        Assertions.assertEquals("blood_test_result_not_found", errorMessage);

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltAfter.getResult(), pltBefore.getResult(), "PLT shouldn't be changed");
    }

    @Test
    void edit_UNAUTHORIZED_test() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                "MTExMTExMTEtMTExMS0xMTExLTExMTEtMTExMTExMTExMTExOjA6RUxlVjBjajk2SnZzNTlvMGkzTWdPZkVveUN6K0hVbHFwbEEvMDJhWjRsYz0=",
                "NDQ0NDQ0NDQtNDQ0NC00NDQ0LTQ0NDQtNDQ0NDQ0NDQ0NDQ0OjA6R0JyZWJNTm9leitYVHJPQUVRMDhNOGMrYWhrN25QdW1OWVVlamtoTmE2az0="
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT should be updated to 255");
    }

    @Test
    @WithMockUser(roles = {"CLIENT"})
    void edit_FORBIDDEN_test() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        LoginDTO loginDTO = new LoginDTO("agorgonzola", "P@ssw0rd!");
        String json = objectMapper.writeValueAsString(loginDTO);

        MvcResult loginResult = mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = loginResult.getResponse().getContentAsString();
        String clientToken = objectMapper.readTree(responseJson).get("value").asText();
        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/account/logout")
                .header("Authorization", "Bearer " + clientToken)).andReturn();

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT should be updated to 255");
    }

    /**
     *  VIOLATIONS - ClientBloodTestReportDTO
     */

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_ClientBloodTestReportID_notNull() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "id": "11111111-1111-1111-1111-111111111111",
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("id"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_ClientBloodTestReportVersion_notNull() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "version": 0,
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("version"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_ClientBloodTestReportClient_notNull() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "client": {
                "id": "00000000-0000-0000-0000-000000000006",
                "dietician_id": "00000000-0000-0000-0000-000000000004",
                },
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_ClientBloodTestReportTimestamp_notNull() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "timestamp": 1749994179,
            "lockToken": "%s",
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("timestamp"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_ClientBloodTestReportResults_Null() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": null
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("results"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_ClientBloodTestReportLockToken_Null() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "results": [
                {
                    "id": null,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(2))
                .andExpect(jsonPath("$.violations[0].fieldName").value("lockToken"))
                .andExpect(jsonPath("$.violations[1].fieldName").value("lockToken"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    /**
     *  VIOLATIONS - BloodTestResultDTO
     */

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_BloodTestResultId_notNull() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "id": "11111111-1111-1111-1111-111111111111",
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("results[0].id"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_BloodTestResultVersion_notNull() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "version": 0,
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("results[0].version"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_BloodTestResultLockToken_Null() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("results[0].lockToken"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_BloodTestResultResult_Null() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "lockToken": "%s",
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("results[0].result"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }


    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_BloodTestResultBloodParameter_Null() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "lockToken": "%s",
                    "result": 255.0
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations.length()").value(1))
                .andExpect(jsonPath("$.violations[0].fieldName").value("results[0].bloodParameter"));

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }

    @Test
    @WithMockUser(roles = {"DIETICIAN"})
    void violation_BloodTestResultReport_notNull() throws Exception {
        ClientBloodTestReport beforeReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltBefore = beforeReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        String payload = """
        {
            "lockToken": "%s",
            "results": [
                {
                    "lockToken": "%s",
                    "result": 255.0,
                    "bloodParameter": {
                        "name": "PLT",
                        "description": "Platelets",
                        "unit": "X10_6_U_L",
                        "standardMin": 150.0,
                        "standardMax": 450.0
                    },
                    report: {
                        "lockToken": "%s",
                        "results": null
                    }
                }
            ]
        }""".formatted(
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue(),
                lockTokenService.generateToken(pltBefore.getId(), pltBefore.getVersion()).getValue(),
                lockTokenService.generateToken(beforeReport.getId(), beforeReport.getVersion()).getValue()
        );

        mockMvc.perform(put("/api/mod/blood-test-reports")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        ClientBloodTestReport afterReport = helper.getClientBloodTestReportById(reportId);
        BloodTestResult pltAfter = afterReport.getResults().stream()
                .filter(r -> "PLT".equals(r.getBloodParameter().name()))
                .findFirst()
                .orElseThrow();

        assertEquals(pltBefore.getResult(), pltAfter.getResult(), "PLT shouldn't be changed");
    }
}

