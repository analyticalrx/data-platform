package com.analyticalrx.dataplatform.scheduler.repository.common;

import com.analyticalrx.dataplatform.scheduler.entity.schema.DrugEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DrugRepository extends JpaRepository<DrugEntity, UUID> {

    List<DrugEntity> findByTenant_Id(UUID tenantId);

    Optional<DrugEntity> findByTenant_IdAndSourceSystemAndSourceDrugId(UUID tenantId, String sourceSystem, String sourceDrugId);

    List<DrugEntity> findByTenant_IdAndNdc(UUID tenantId, String ndc);
}