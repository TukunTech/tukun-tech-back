package com.upc.tukuntech.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.tukuntech.backend.modules.auth.domain.entity.PermissionEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.RoleEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;
import com.upc.tukuntech.backend.modules.auth.domain.repository.PermissionRepository;
import com.upc.tukuntech.backend.modules.auth.domain.repository.RoleRepository;
import com.upc.tukuntech.backend.modules.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración: gates por rol (ADMIN / ATTENDANT / PATIENT)")
class RoleGatesIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PermissionRepository permissionRepository;
    @Autowired
    UserRepository userRepository;

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @BeforeEach
    void seedRolesIfMissing() {
        if (roleRepository.findByName("ADMINISTRATOR").isEmpty()) {
            RoleEntity admin = new RoleEntity();
            admin.setName("ADMINISTRATOR");
            PermissionEntity perm = new PermissionEntity();
            perm.setName("PERM_ADMIN_DASHBOARD");
            permissionRepository.save(perm);
            admin.getPermissions().add(perm);
            roleRepository.save(admin);
        }
        if (roleRepository.findByName("ATTENDANT").isEmpty()) {
            RoleEntity attendant = new RoleEntity();
            attendant.setName("ATTENDANT");
            roleRepository.save(attendant);
        }
        if (roleRepository.findByName("PATIENT").isEmpty()) {
            RoleEntity patient = new RoleEntity();
            patient.setName("PATIENT");
            roleRepository.save(patient);
        }
    }

    private static String uniqueDni() {
        long n = System.nanoTime() % 9_000_000L + 10_000_000L;
        return Long.toString(n);
    }

    private String tokenWithRole(String email, String desiredRole) throws Exception {
        String roleForRegister = switch (desiredRole) {
            case "PATIENT", "ATTENDANT" -> desiredRole;
            default -> "PATIENT";
        };

        String registerBody = """
                {
                  "firstName": "Gate",
                  "lastName": "%s",
                  "dni": "%s",
                  "email": "%s",
                  "password": "Secret0!",
                  "role": "%s",
                  "gender": "FEMALE",
                  "age": 25,
                  "bloodGroup": "O_POSITIVE",
                  "nationality": "PERUVIAN",
                  "allergy": "PENICILLIN"
                }
                """.formatted(desiredRole, uniqueDni(), email, roleForRegister);

        mvc.perform(post("/auth/register")
                        .contentType(JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        if ("ADMINISTRATOR".equals(desiredRole)) {
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found after register"));
            RoleEntity admin = roleRepository.findByName("ADMINISTRATOR")
                    .orElseThrow(() -> new IllegalStateException("Role not seeded: ADMINISTRATOR"));
            user.getRoles().clear();
            user.getRoles().add(admin);
            userRepository.save(user);
        }

        String loginBody = """
                {
                  "email": "%s",
                  "password": "Secret0!"
                }
                """.formatted(email);

        var loginRes = mvc.perform(post("/auth/login")
                        .contentType(JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        var json = om.readTree(loginRes.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    @Test
    @DisplayName("ADMINISTRATOR → /test/admin/ping = 200; PATIENT → 403; sin token → 401")
    void adminGate_ShouldAllowAdmin_AndBlockOthers() throws Exception {
        // Arrange
        String adminToken = tokenWithRole("admin.gate@example.com", "ADMINISTRATOR");
        String patientToken = tokenWithRole("patient.gate@example.com", "PATIENT");

        // Act + Assert
        mvc.perform(get("/test/admin/ping")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mvc.perform(get("/test/admin/ping")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/test/admin/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ATTENDANT → /test/attendant/ping = 200; PATIENT → 403")
    void attendantGate_ShouldAllowAttendant_AndBlockPatient() throws Exception {
        String attendantToken = tokenWithRole("att.gate@example.com", "ATTENDANT");
        String patientToken = tokenWithRole("patient2.gate@example.com", "PATIENT");

        mvc.perform(get("/test/attendant/ping")
                        .header("Authorization", "Bearer " + attendantToken))
                .andExpect(status().isOk());

        mvc.perform(get("/test/attendant/ping")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATIENT → /test/patient/ping = 200; ATTENDANT → 403")
    void patientGate_ShouldAllowPatient_AndBlockAttendant() throws Exception {
        String patientToken = tokenWithRole("patient3.gate@example.com", "PATIENT");
        String attendantToken = tokenWithRole("att2.gate@example.com", "ATTENDANT");

        mvc.perform(get("/test/patient/ping")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk());

        mvc.perform(get("/test/patient/ping")
                        .header("Authorization", "Bearer " + attendantToken))
                .andExpect(status().isForbidden());
    }
}