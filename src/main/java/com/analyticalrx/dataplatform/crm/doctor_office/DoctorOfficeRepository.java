package com.analyticalrx.dataplatform.crm.doctor_office;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorOfficeRepository extends JpaRepository<DoctorOffice, UUID> {

    List<DoctorOffice> findByRelationshipStage(RelationshipStage stage);

    List<DoctorOffice> findByOwnerId(UUID ownerId);

    List<DoctorOffice> findByStoreId(UUID storeId);

    List<DoctorOffice> findBySpecialtyIgnoreCase(String specialty);
}