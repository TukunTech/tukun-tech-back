package com.upc.tukuntech.backend.auth;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integración CORS: preflight permitido y denegado")
class CorsIntegrationTest {

    @Autowired
    MockMvc mvc;

    private static final String ALLOWED_ORIGIN = "http://localhost:4200";
    private static final String DISALLOWED_ORIGIN = "http://evil.com";

    @Test
    @DisplayName("Preflight permitido para Origin permitido")
    void should_AllowCorsPreflight_ForAllowedOrigin() throws Exception {
        mvc.perform(options("/auth/login")
                        .header("Origin", ALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", HttpMethod.POST.name())
                        .header("Access-Control-Request-Headers", "authorization,content-type")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Methods", Matchers.containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers", Matchers.allOf(
                        Matchers.containsStringIgnoringCase("authorization"),
                        Matchers.containsStringIgnoringCase("content-type")
                )))
                .andExpect(header().string("Vary", Matchers.anyOf(
                        Matchers.containsString("Origin"),
                        Matchers.any(String.class)
                )));
    }


    @Test
    @DisplayName("Preflight denegado para Origin NO permitido")
    void should_DenyCorsPreflight_ForDisallowedOrigin() throws Exception {
        mvc.perform(options("/auth/login")
                        .header("Origin", DISALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", HttpMethod.POST.name())
                        .header("Access-Control-Request-Headers", "authorization,content-type")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Methods"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Headers"));
    }

}