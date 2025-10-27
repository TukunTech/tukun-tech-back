package com.upc.tukuntech.backend.modules.support.domain.repositories;

import com.upc.tukuntech.backend.modules.support.domain.entity.SupportResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportResponseRepository extends JpaRepository<SupportResponse, Long> {
}
