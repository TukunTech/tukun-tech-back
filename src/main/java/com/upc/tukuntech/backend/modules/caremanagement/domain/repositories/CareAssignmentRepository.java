package com.upc.tukuntech.backend.modules.caremanagement.domain.repositories;

import com.upc.tukuntech.backend.modules.caremanagement.domain.entity.CareAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareAssignmentRepository extends JpaRepository<CareAssignment, Long> {

    // Pacientes de un cuidador
    List<CareAssignment> findByCaregiverId(Long caregiverId);

    // Cuidadores de un paciente
    List<CareAssignment> findByPatientId(Long patientId);

    // Para evitar duplicados
    Optional<CareAssignment> findByCaregiverIdAndPatientId(Long caregiverId, Long patientId);

    void deleteByCaregiverIdAndPatientId(Long caregiverId, Long patientId);
}

