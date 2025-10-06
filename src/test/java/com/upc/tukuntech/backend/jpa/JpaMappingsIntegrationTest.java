package com.upc.tukuntech.backend.jpa;

import com.upc.tukuntech.backend.modules.auth.domain.entity.PermissionEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.RoleEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;
import com.upc.tukuntech.backend.modules.auth.domain.model.Allergy;
import com.upc.tukuntech.backend.modules.auth.domain.model.BloodGroup;
import com.upc.tukuntech.backend.modules.auth.domain.model.Gender;
import com.upc.tukuntech.backend.modules.auth.domain.model.Nationality;
import com.upc.tukuntech.backend.modules.auth.domain.repository.PermissionRepository;
import com.upc.tukuntech.backend.modules.auth.domain.repository.RoleRepository;
import com.upc.tukuntech.backend.modules.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.EntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@DisplayName("JPA: mapeos User ↔ Role ↔ Permission + unicidad")
class JpaMappingsIntegrationTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    EntityManager em;

    private static String uniqueDni() {
        long n = System.nanoTime() % 9_000_000L + 10_000_000L;
        return Long.toString(n);
    }

    private UserEntity buildUser(String email, String dni) {
        UserEntity u = new UserEntity();
        u.setFirstName("Ana");
        u.setLastName("Pérez");
        u.setDni(dni);
        u.setEmail(email);
        String hashed = new BCryptPasswordEncoder().encode("Secret0!");
        u.setPassword(hashed);
        u.setGender(Gender.FEMALE);
        u.setAge(25);
        u.setBloodGroup(BloodGroup.O_POSITIVE);
        u.setNationality(Nationality.PERUVIAN);
        u.setAllergy(Allergy.PENICILLIN);
        u.setEnabled(true);
        return u;
    }

    @Test
    @DisplayName("Debe persistir y recuperar User con Roles y Permisos (EAGER sin LazyException)")
    void should_PersistAndLoad_UserWithRolesAndPermissions() {
        // Arrange (AAA)
        PermissionEntity pRead = new PermissionEntity();
        pRead.setName("PERM_USER_READ");
        PermissionEntity pWrite = new PermissionEntity();
        pWrite.setName("PERM_USER_WRITE");
        permissionRepository.save(pRead);
        permissionRepository.save(pWrite);

        RoleEntity role = new RoleEntity();
        role.setName("PATIENT");
        role.getPermissions().add(pRead);
        role.getPermissions().add(pWrite);
        roleRepository.save(role);

        UserEntity user = buildUser("ana.jpa@example.com", uniqueDni());
        user.getRoles().add(role);
        userRepository.saveAndFlush(user);

        em.clear();

        // Act
        UserEntity loaded = userRepository.findByEmail("ana.jpa@example.com")
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Assert
        assertThat(loaded.getId()).isNotNull();
        assertThat(loaded.getRoles()).hasSize(1);
        RoleEntity loadedRole = loaded.getRoles().iterator().next();
        assertThat(loadedRole.getName()).isEqualTo("PATIENT");
        assertThat(loadedRole.getPermissions()).extracting(PermissionEntity::getName)
                .containsExactlyInAnyOrder("PERM_USER_READ", "PERM_USER_WRITE");
        assertThat(loaded.getDni()).hasSize(8);
        assertThat(loaded.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(loaded.getBloodGroup()).isEqualTo(BloodGroup.O_POSITIVE);
        assertThat(loaded.getNationality()).isEqualTo(Nationality.PERUVIAN);
        assertThat(loaded.getAllergy()).isEqualTo(Allergy.PENICILLIN);
        assertThat(loaded.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("Debe hacer cumplir unicidad de email")
    void should_EnforceUniqueEmail() {
        // Arrange
        String email = "unique.email@example.com";
        UserEntity u1 = buildUser(email, uniqueDni());
        userRepository.save(u1);
        userRepository.flush();

        UserEntity u2 = buildUser(email, uniqueDni());

        // Act + Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(u2);
        });
    }

    @Test
    @DisplayName("Debe hacer cumplir unicidad de DNI")
    void should_EnforceUniqueDni() {
        // Arrange
        String dni = uniqueDni();
        UserEntity u1 = buildUser("dni.1@example.com", dni);
        userRepository.save(u1);
        userRepository.flush();

        UserEntity u2 = buildUser("dni.2@example.com", dni);

        // Act + Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(u2);
        });
    }
}