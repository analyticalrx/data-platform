package com.analyticalrx.dataplatform.scheduler.repository.common;

import com.analyticalrx.dataplatform.scheduler.entity.schema.PharmacyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PharmacyRepository extends JpaRepository<PharmacyEntity, UUID> {

    Optional<PharmacyEntity> findByTenant_IdAndPharmacyKey(UUID tenantId, String pharmacyKey);

    List<PharmacyEntity> findByTenant_Id(UUID tenantId);

    boolean existsByTenant_IdAndPharmacyKey(UUID tenantId, String pharmacyKey);
}