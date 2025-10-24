package com.upc.tukuntech.backend.modules.profiles.domain.entity;

import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Allergy;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.BloodGroup;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Gender;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Nationality;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profiles",
        indexes = {
                @Index(name = "profiles_dni", columnList = "dni", unique = true)
        })
@Getter
@Setter
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // referencia al IAM.UserIdentity.id

    @Column(length = 80)
    private String firstName;

    @Column(length = 80)
    private String lastName;

    @Column(length = 20, unique = true)
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
}
