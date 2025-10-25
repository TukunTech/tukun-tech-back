package com.upc.tukuntech.backend.modules.profiles.domain.entity;

import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profiles",
        indexes = {
                @Index(name = "profiles_dni", columnList = "dni", unique = true),
                @Index(name = "profiles_user", columnList = "userId", unique = true)
        })
@Getter @Setter
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // referencia al IAM.UserIdentity.id

    @Column(length = 80, nullable = false)
    private String firstName;

    @Column(length = 80, nullable = false)
    private String lastName;

    @Column(length = 20, unique = true, nullable = false)
    private String dni;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.OTHER;

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    private Nationality nationality;

    @Enumerated(EnumType.STRING)
    private Allergy allergy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProfileType profileType; // 🔹 Nuevo campo
}
