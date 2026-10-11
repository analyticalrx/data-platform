package com.analyticalrx.dataplatform.scheduler.repository.common;

import com.analyticalrx.dataplatform.scheduler.entity.schema.PayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayerRepository extends JpaRepository<PayerEntity, UUID> {

    List<PayerEntity> findByTenant_Id(UUID tenantId);

    List<PayerEntity> findByTenant_IdAndSourceSystem(UUID tenantId, String sourceSystem);

    List<PayerEntity> findByTenant_IdAndSourceSystemAndSourcePayerId(UUID tenantId, String sourceSystem, String sourcePayerId);
}