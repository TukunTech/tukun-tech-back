package com.upc.tukuntech.backend.config;

import com.upc.tukuntech.backend.modules.auth.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupChecks {

    private static final Logger log = LoggerFactory.getLogger(StartupChecks.class);

    @Bean
    public CommandLineRunner checkRoles(RoleRepository roleRepository) {
        return args -> {
            log.info("=== Startup check: roles in DB ===");
            var roles = roleRepository.findAll();
            if (roles == null || roles.isEmpty()) {
                log.warn("No roles found in DB!");
            } else {
                roles.forEach(r -> log.info("Role -> id: {}, name: {}", r.getId(), r.getName()));
            }
            log.info("=== End startup check ===");
        };
    }
}

