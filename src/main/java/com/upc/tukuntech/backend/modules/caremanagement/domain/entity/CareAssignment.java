package com.upc.tukuntech.backend.modules.caremanagement.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "care_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_caregiver_patient", columnNames = {"caregiver_id", "patient_id"})
        },
        indexes = {
                @Index(name = "idx_caregiver_id", columnList = "caregiver_id"),
                @Index(name = "idx_patient_id", columnList = "patient_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class CareAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caregiver_id", nullable = false)
    private Long caregiverId; // userId del cuidador (ATTENDANT)

    @Column(name = "patient_id", nullable = false)
    private Long patientId; // userId del paciente (PATIENT)

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    // ✅ Método de fábrica (en lugar de constructor directo)
    public static CareAssignment assign(Long caregiverId, Long patientId) {
        CareAssignment assignment = new CareAssignment();
        assignment.setCaregiverId(caregiverId);
        assignment.setPatientId(patientId);
        assignment.setAssignedAt(LocalDateTime.now());
        return assignment;
    }
}
