package com.upc.tukuntech.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.tukuntech.backend.modules.auth.domain.entity.PermissionEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.RoleEntity;

import com.upc.tukuntech.backend.modules.auth.domain.repository.PermissionRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración: ciclo de tokens (refresh / logout)")
class TokenLifecycleIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PermissionRepository permissionRepository;

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @BeforeEach
    void seedPatientRoleIfMissing() {
        if (roleRepository.findByName("PATIENT").isEmpty()) {
            RoleEntity role = new RoleEntity();
            role.setName("PATIENT");
            PermissionEntity pRead = new PermissionEntity();
            pRead.setName("PERM_USER_READ");
            PermissionEntity pWrite = new PermissionEntity();
            pWrite.setName("PERM_USER_WRITE");
            permissionRepository.save(pRead);
            permissionRepository.save(pWrite);
            role.getPermissions().add(pRead);
            role.getPermissions().add(pWrite);
            roleRepository.save(role);
        }
    }

    private static String registerBody(String email) {
        return """
                {
                  "firstName": "Tok",
                  "lastName": "Cycle",
                  "dni": "99887766",
                  "email": "%s",
                  "password": "Secret0!",
                  "role": "PATIENT",
                  "gender": "FEMALE",
                  "age": 25,
                  "bloodGroup": "O_POSITIVE",
                  "nationality": "PERUVIAN",
                  "allergy": "PENICILLIN"
                }
                """.formatted(email);
    }

    private static String loginBody(String email) {
        return """
                {
                  "email": "%s",
                  "password": "Secret0!"
                }
                """.formatted(email);
    }

    @Test
    @DisplayName("Refresh válido devuelve nuevo access; después de logout el refresh falla (401)")
    void should_RefreshAndThenFailAfterLogout() throws Exception {
        // Arrange
        String email = "tokcycle@example.com";

        mvc.perform(post("/auth/register")
                        .contentType(JSON).content(registerBody(email)))
                .andExpect(status().isOk());

        var loginRes = mvc.perform(post("/auth/login")
                        .contentType(JSON).content(loginBody(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        var loginJson = om.readTree(loginRes.getResponse().getContentAsString());
        String refreshToken = loginJson.get("refreshToken").asText();

        // Act
        mvc.perform(post("/auth/refresh")
                        .contentType(JSON)
                        .content(om.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // Act
        mvc.perform(post("/auth/logout")
                        .contentType(JSON)
                        .content(om.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk());

        // Assert
        mvc.perform(post("/auth/refresh")
                        .contentType(JSON)
                        .content(om.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh con token alterado debe devolver 401")
    void should_Return401_When_RefreshTokenIsTampered() throws Exception {
        // Arrange
        String email = "tampered@example.com";

        mvc.perform(post("/auth/register")
                        .contentType(JSON).content(registerBody(email)))
                .andExpect(status().isOk());

        var loginRes = mvc.perform(post("/auth/login")
                        .contentType(JSON).content(loginBody(email)))
                .andExpect(status().isOk())
                .andReturn();

        var loginJson = om.readTree(loginRes.getResponse().getContentAsString());
        String refreshToken = loginJson.get("refreshToken").asText();
        String tampered = refreshToken + "x";

        // Act + Assert
        mvc.perform(post("/auth/refresh")
                        .contentType(JSON)
                        .content(om.writeValueAsString(java.util.Map.of("refreshToken", tampered))))
                .andExpect(status().isUnauthorized());
    }
}