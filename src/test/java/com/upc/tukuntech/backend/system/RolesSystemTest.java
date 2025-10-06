package com.upc.tukuntech.backend.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.tukuntech.backend.modules.auth.domain.entity.RoleEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;
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
@DisplayName("System: Gates por rol (/test/*/ping)")
class RolesSystemTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    private static String dni() {
        long n = System.nanoTime() % 9_000_000L + 10_000_000L;
        return Long.toString(n);
    }

    @BeforeEach
    void seedRoles() {
        if (roleRepository.findByName("ADMINISTRATOR").isEmpty()) {
            RoleEntity r = new RoleEntity();
            r.setName("ADMINISTRATOR");
            roleRepository.save(r);
        }
        if (roleRepository.findByName("ATTENDANT").isEmpty()) {
            RoleEntity r = new RoleEntity();
            r.setName("ATTENDANT");
            roleRepository.save(r);
        }
        if (roleRepository.findByName("PATIENT").isEmpty()) {
            RoleEntity r = new RoleEntity();
            r.setName("PATIENT");
            roleRepository.save(r);
        }
    }

    private String register(String email, String role) throws Exception {
        String body = """
                {
                  "firstName": "U",
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
                """.formatted(role, dni(), email, role);

        mvc.perform(post("/auth/register").contentType(JSON).content(body))
                .andExpect(status().isOk());
        return email;
    }

    private String login(String email) throws Exception {
        String body = """
                {
                  "email": "%s",
                  "password": "Secret0!"
                }
                """.formatted(email);

        var res = mvc.perform(post("/auth/login").contentType(JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();

        return om.readTree(res.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    @DisplayName("Admin 200 / Patient 403 / Sin token 401 en /test/admin/ping")
    void gates_admin_ping() throws Exception {
        // Arrange (AAA): registrar como PATIENT y luego elevar a ADMINISTRATOR en BD
        String adminEmail = register("sys.admin@example.com", "PATIENT");
        UserEntity u = userRepository.findByEmail(adminEmail).orElseThrow();
        u.getRoles().clear();
        u.getRoles().add(roleRepository.findByName("ADMINISTRATOR").orElseThrow());
        userRepository.save(u);

        String patientEmail = register("sys.patient@example.com", "PATIENT");

        // Act: login y obtener tokens
        String adminToken = login(adminEmail);
        String patientToken = login(patientEmail);

        // Assert
        mvc.perform(get("/test/admin/ping").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mvc.perform(get("/test/admin/ping").header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/test/admin/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Attendant 200 / Patient 403 en /test/attendant/ping")
    void gates_attendant_ping() throws Exception {
        // Arrange
        String attEmail = register("sys.attendant@example.com", "ATTENDANT");
        String patEmail = register("sys.patient2@example.com", "PATIENT");

        String attToken = login(attEmail);
        String patToken = login(patEmail);

        // Assert
        mvc.perform(get("/test/attendant/ping").header("Authorization", "Bearer " + attToken))
                .andExpect(status().isOk());

        mvc.perform(get("/test/attendant/ping").header("Authorization", "Bearer " + patToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Patient 200 / Attendant 403 en /test/patient/ping")
    void gates_patient_ping() throws Exception {
        // Arrange
        String patEmail = register("sys.patient3@example.com", "PATIENT");
        String attEmail = register("sys.attendant2@example.com", "ATTENDANT");

        String patToken = login(patEmail);
        String attToken = login(attEmail);

        // Assert
        mvc.perform(get("/test/patient/ping").header("Authorization", "Bearer " + patToken))
                .andExpect(status().isOk());

        mvc.perform(get("/test/patient/ping").header("Authorization", "Bearer " + attToken))
                .andExpect(status().isForbidden());
    }
}