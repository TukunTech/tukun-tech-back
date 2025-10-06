package com.upc.tukuntech.backend.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.tukuntech.backend.modules.auth.domain.entity.RoleEntity;
import com.upc.tukuntech.backend.modules.auth.domain.repository.RoleRepository;
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
@DisplayName("System: Auth E2E (register → login → me → refresh → logout → me401)")
class AuthSystemTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    RoleRepository roleRepository;

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    private static String dni() {
        long n = System.nanoTime() % 9_000_000L + 10_000_000L;
        return Long.toString(n);
    }

    @BeforeEach
    void seedRolesIfMissing() {
        if (roleRepository.findByName("PATIENT").isEmpty()) {
            RoleEntity r = new RoleEntity();
            r.setName("PATIENT");
            roleRepository.save(r);
        }
        if (roleRepository.findByName("ATTENDANT").isEmpty()) {
            RoleEntity r = new RoleEntity();
            r.setName("ATTENDANT");
            roleRepository.save(r);
        }
        if (roleRepository.findByName("ADMINISTRATOR").isEmpty()) {
            RoleEntity r = new RoleEntity();
            r.setName("ADMINISTRATOR");
            roleRepository.save(r);
        }
    }

    @Test
    @DisplayName("Flujo completo de autenticación y sesiones")
    void auth_full_flow() throws Exception {
        // Arrange: payloads
        String email = "sys.ana@example.com";
        String registerBody = """
                  {
                    "firstName":"Ana","lastName":"Pérez","dni":"%s","email":"%s","password":"Secret0!",
                    "role":"PATIENT","gender":"FEMALE","age":25,
                    "bloodGroup":"O_POSITIVE","nationality":"PERUVIAN","allergy":"PENICILLIN"
                  }
                """.formatted(dni(), email);

        String loginBody = """
                  {"email":"%s","password":"Secret0!"}
                """.formatted(email);

        mvc.perform(post("/auth/register").contentType(JSON).content(registerBody))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(JSON));

        var loginRes = mvc.perform(post("/auth/login").contentType(JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        JsonNode loginJson = om.readTree(loginRes.getResponse().getContentAsString());
        String access1 = loginJson.get("accessToken").asText();
        String refresh1 = loginJson.get("refreshToken").asText();

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + access1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());

        String refreshBody = """
                  {"refreshToken":"%s"}
                """.formatted(refresh1);
        var refreshRes = mvc.perform(post("/auth/refresh").contentType(JSON).content(refreshBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        JsonNode refreshJson = om.readTree(refreshRes.getResponse().getContentAsString());
        String access2 = refreshJson.get("accessToken").asText();
        String refresh2 = refreshJson.has("refreshToken") ? refreshJson.get("refreshToken").asText() : refresh1;

        String logoutBody = """
                  {"refreshToken":"%s"}
                """.formatted(refresh2);

        mvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + access2)
                        .contentType(JSON)
                        .content(logoutBody))
                .andExpect(status().isOk());

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + access2))
                .andExpect(status().isOk());

        mvc.perform(post("/auth/refresh")
                        .contentType(JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refresh2)))
                .andExpect(status().isUnauthorized());

    }
}
