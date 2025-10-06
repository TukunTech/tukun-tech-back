package com.upc.tukuntech.backend.modules.auth.domain.entity;

import com.upc.tukuntech.backend.modules.auth.domain.model.Allergy;
import com.upc.tukuntech.backend.modules.auth.domain.model.BloodGroup;
import com.upc.tukuntech.backend.modules.auth.domain.model.Gender;
import com.upc.tukuntech.backend.modules.auth.domain.model.Nationality;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "users_email", columnList = "email", unique = true),
                @Index(name = "users_dni", columnList = "dni", unique = true)
        }
)
@Getter
@Setter
@ToString(exclude = "password")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Size(max = 80)
    @NotBlank
    private String firstName;

    @Size(max = 80)
    @NotBlank
    private String lastName;

    @Column(length = 20, nullable = false, unique = true)
    @NotBlank
    @Size(min = 8, max = 20)
    private String dni;

    @Column(nullable = false, unique = true, length = 160)
    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @Column(nullable = false, length = 120)
    @NotBlank
    @Size(min = 60, max = 120)
    @Pattern(regexp = "^\\$2[aby]?\\$\\d\\d\\$[./A-Za-z0-9]{53,}$",
            message = "Password debe ser un hash BCrypt válido")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @NotNull
    private Gender gender = Gender.OTHER;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    @Max(120)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private Nationality nationality;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = true)
    private Allergy allergy;

    @Column(nullable = false)
    @NotNull
    private Boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (email != null) email = email.trim().toLowerCase(Locale.ROOT);
        if (firstName != null) firstName = firstName.trim();
        if (lastName != null) lastName = lastName.trim();
        if (dni != null) dni = dni.trim();
    }
}