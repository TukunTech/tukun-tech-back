package com.upc.tukuntech.backend.modules.iam.infrastructure.seeding;



import com.upc.tukuntech.backend.modules.iam.domain.entity.RoleEntity;
import com.upc.tukuntech.backend.modules.iam.domain.entity.UserIdentity;
import com.upc.tukuntech.backend.modules.iam.domain.repositories.PermissionRepository;
import com.upc.tukuntech.backend.modules.iam.domain.repositories.RoleRepository;
import com.upc.tukuntech.backend.modules.iam.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.upc.tukuntech.backend.modules.iam.domain.entity.PermissionEntity;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class DevSeed implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeed.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        log.info("🌱 Running IAM DevSeed: initializing default roles, permissions and users...");

        // --- Roles ---
        var admin = roleRepository.findByName("ADMINISTRATOR").orElseGet(() -> {
            var r = new RoleEntity();
            r.setName("ADMINISTRATOR");
            return roleRepository.save(r);
        });

        var attendant = roleRepository.findByName("ATTENDANT").orElseGet(() -> {
            var r = new RoleEntity();
            r.setName("ATTENDANT");
            return roleRepository.save(r);
        });

        var patient = roleRepository.findByName("PATIENT").orElseGet(() -> {
            var r = new RoleEntity();
            r.setName("PATIENT");
            return roleRepository.save(r);
        });

        // --- Permissions ---
        var pRead = permissionRepository.findByName("PATIENT_READ")
                .orElseGet(() -> permissionRepository.save(newPerm("PATIENT_READ")));

        var pWrite = permissionRepository.findByName("PATIENT_WRITE")
                .orElseGet(() -> permissionRepository.save(newPerm("PATIENT_WRITE")));

        admin.getPermissions().addAll(Set.of(pRead, pWrite));
        attendant.getPermissions().add(pRead);

        roleRepository.save(admin);
        roleRepository.save(attendant);
        roleRepository.save(patient);

        // --- Default Users ---
        userRepository.findByEmail("admin@tukuntech.com").orElseGet(() -> {
            var u = new UserIdentity();
            u.setEmail("admin@tukuntech.com");
            u.setPassword(encoder.encode("Admin123"));
            u.setEnabled(true);
            u.getRoles().add(admin);
            return userRepository.save(u);
        });
        log.info("👤 User created: admin@tukuntech.com / Pss: Admin123");

        userRepository.findByEmail("attendant@tukuntech.com").orElseGet(() -> {
            var u = new UserIdentity();
            u.setEmail("attendant@tukuntech.com");
            u.setPassword(encoder.encode("Attendant123"));
            u.setEnabled(true);
            u.getRoles().add(attendant);
            return userRepository.save(u);
        });
        log.info("👤 User created: attendant@tukuntech.com / Pss: Attendant123");

        userRepository.findByEmail("patient@tukuntech.com").orElseGet(() -> {
            var u = new UserIdentity();
            u.setEmail("patient@tukuntech.com");
            u.setPassword(encoder.encode("Patient123"));
            u.setEnabled(true);
            u.getRoles().add(patient);
            return userRepository.save(u);
        });
        log.info("👤 User created: patient@tukuntech.com / Pss: Patient123");

        log.info("✅ IAM seeding completed successfully.");
    }

    private static PermissionEntity newPerm(String name) {
        var perm = new PermissionEntity();
        perm.setName(name);
        return perm;
    }
}
