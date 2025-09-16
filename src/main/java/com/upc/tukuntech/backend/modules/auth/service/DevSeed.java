package com.upc.tukuntech.backend.modules.auth.service;

import com.upc.tukuntech.backend.modules.auth.entity.PermissionEntity;
import com.upc.tukuntech.backend.modules.auth.entity.RoleEntity;
import com.upc.tukuntech.backend.modules.auth.entity.UserEntity;
import com.upc.tukuntech.backend.modules.auth.repository.PermissionRepository;
import com.upc.tukuntech.backend.modules.auth.repository.RoleRepository;
import com.upc.tukuntech.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class DevSeed implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        var admin = roleRepository.findByName("ADMINISTRATOR").orElseGet(() -> {
            var r = new RoleEntity(); r.setName("ADMINISTRATOR"); return roleRepository.save(r);
        });

        var attendant = roleRepository.findByName("ATTENDANT").orElseGet(() -> {
            var r = new RoleEntity(); r.setName("ATTENDANT"); return roleRepository.save(r);
        });

        var patient = roleRepository.findByName("PATIENT").orElseGet(() -> {
            var r = new RoleEntity(); r.setName("PATIENT"); return roleRepository.save(r);
        });

        var pRead = permissionRepository.findByName("PATIENT_READ").orElseGet(() -> permissionRepository.save(newPerm("PATIENT_READ")));

        var pWrite = permissionRepository.findByName("PATIENT_WRITE").orElseGet(() -> permissionRepository.save(newPerm("PATIENT_WRITE")));

        admin.getPermissions().addAll(Set.of(pRead, pWrite));
        attendant.getPermissions().add(pRead);
        roleRepository.save(admin);
        roleRepository.save(attendant);
        roleRepository.save(patient);

        userRepository.findByEmail("admin@tukuntech.com").orElseGet(()->{
            var u = new UserEntity();
            u.setFirstName("Admin");
            u.setLastName("Admin");
            u.setDni("123456789");
            u.setEmail("admin@tukuntech.com");
            u.setPassword(encoder.encode("Admin123"));
            u.setEnabled(true);
            u.getRoles().add(admin);
            return userRepository.save(u);
        });
        System.out.println("User: admin@tukuntech.com / Pss: Admin123");
    }

    private static PermissionEntity newPerm(String name) {
        var perm = new PermissionEntity();
        perm.setName(name);
        return perm;
    }

}
