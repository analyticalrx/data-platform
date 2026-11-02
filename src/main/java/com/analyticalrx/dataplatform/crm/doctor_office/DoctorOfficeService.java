package com.analyticalrx.dataplatform.crm.doctor_office;

import java.util.List;
import java.util.UUID;

public interface DoctorOfficeService {
    DoctorOfficeResponseDTO createOffice(DoctorOfficeRequestDTO requestDTO);
    DoctorOfficeResponseDTO updateOffice(UUID id, DoctorOfficeRequestDTO requestDTO);
    DoctorOfficeResponseDTO getOfficeById(UUID id);
    List<DoctorOfficeResponseDTO> getAllOffices(RelationshipStage stage, UUID ownerId, UUID storeId, String specialty);
    void deleteOffice(UUID id);
}