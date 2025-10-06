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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración Auth: register → login → me ")
public class AuthIntegrationTest {

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

    @Test
    @DisplayName("GoodWay debe registrar, autenticar y devolver perfil con Bearer")
    void should_RegisterLoginAndGetMe() throws Exception {
        // Arrange
        String registerBody = """
                {
                  "firstName": "Ana",
                  "lastName": "Pérez",
                  "dni": "12345678",
                  "email": "ana@example.com",
                  "password": "Secret0!",
                  "role": "PATIENT",
                  "gender": "FEMALE",
                  "age": 25,
                  "bloodGroup": "O_POSITIVE",
                  "nationality": "PERUVIAN",
                  "allergy": "PENICILLIN"
                }
                """;

        String loginBody = """
                {
                  "email": "ana@example.com",
                  "password": "Secret0!"
                }
                """;

        // Act: register
        mvc.perform(post("/auth/register").contentType(JSON).content(registerBody))
                .andExpect(status().isOk());

        // Act: login
        var loginRes = mvc.perform(post("/auth/login").contentType(JSON).content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String accessToken = om.readTree(loginRes.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Act + Assert
        mvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))              // ojo: viene como string
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.bloodGroup").value("O_POSITIVE"))
                .andExpect(jsonPath("$.nationality").value("PERUVIAN"))
                .andExpect(jsonPath("$.allergy").value("PENICILLIN"));

    }

    @Test
    @DisplayName("Login con contraseña incorrecta debe devolver 401 y ApiError")
    void should_Return401_When_InvalidCredentials() throws Exception {
        // Arrange
        String registerBody = """
                {
                  "firstName": "A",
                  "lastName": "B",
                  "dni": "11112222",
                  "email": "badpass@example.com",
                  "password": "Correcta1!",
                  "gender": "FEMALE",
                  "age": 21,
                  "bloodGroup": "O_POSITIVE",
                  "nationality": "PERUVIAN",
                  "role": "PATIENT"
                }
                """;

        String badLoginBody = """
                {
                  "email": "badpass@example.com",
                  "password": "incorrecta"
                }
                """;

        mvc.perform(post("/auth/register")
                        .contentType(JSON)
                        .content(registerBody))
                .andExpect(status().isOk()); // <- antes isCreated()

        // Act + Assert
        mvc.perform(post("/auth/login")
                        .contentType(JSON)
                        .content(badLoginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists());
    }
}
