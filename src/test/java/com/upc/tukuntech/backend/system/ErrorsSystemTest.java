package com.upc.tukuntech.backend.system;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("System: Errores comunes (401, 400/409)")
class ErrorsSystemTest {

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
    void seedRoles() {
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
    @DisplayName("Login con password incorrecta → 401")
    void login_bad_password_401() throws Exception {
        String email = "sys.err@example.com";
        String registerBody = """
                {
                  "firstName": "A",
                  "lastName": "B",
                  "dni": "%s",
                  "email": "%s",
                  "password": "Correcta1!",
                  "role": "PATIENT",
                  "gender": "FEMALE",
                  "age": 21,
                  "bloodGroup": "O_POSITIVE",
                  "nationality": "PERUVIAN",
                  "allergy": "PENICILLIN"
                }
                """.formatted(dni(), email);

        mvc.perform(post("/auth/register").contentType(JSON).content(registerBody))
                .andExpect(status().isOk());

        String badLogin = """
                {
                  "email": "%s",
                  "password": "incorrecta"
                }
                """.formatted(email);

        mvc.perform(post("/auth/login").contentType(JSON).content(badLogin))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registro con email duplicado → 400/409")
    void register_duplicate_email_returns_400_or_409() throws Exception {
        String email = "sys.dup@example.com";

        String body1 = """
                {
                  "firstName": "X",
                  "lastName": "Y",
                  "dni": "%s",
                  "email": "%s",
                  "password": "Secret0!",
                  "role": "PATIENT",
                  "gender": "FEMALE",
                  "age": 25,
                  "bloodGroup": "O_POSITIVE",
                  "nationality": "PERUVIAN",
                  "allergy": "PENICILLIN"
                }
                """.formatted(dni(), email);

        String body2 = """
                {
                  "firstName": "X",
                  "lastName": "Y",
                  "dni": "%s",
                  "email": "%s",
                  "password": "Secret0!",
                  "role": "PATIENT",
                  "gender": "FEMALE",
                  "age": 25,
                  "bloodGroup": "O_POSITIVE",
                  "nationality": "PERUVIAN",
                  "allergy": "PENICILLIN"
                }
                """.formatted(dni(), email);

        mvc.perform(post("/auth/register").contentType(JSON).content(body1))
                .andExpect(status().isOk());

        var dup = mvc.perform(post("/auth/register").contentType(JSON).content(body2))
                .andReturn();

        int sc = dup.getResponse().getStatus();
        assertThat(sc).isIn(400, 409);
    }
}