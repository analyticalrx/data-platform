package com.analyticalrx.dataplatform.crm.doctor_office;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorOfficeServiceImpl implements DoctorOfficeService {

    private final DoctorOfficeRepository repository;

    @Autowired
    public DoctorOfficeServiceImpl(DoctorOfficeRepository repository) {
        this.repository = repository;
    }

    @Override
    public DoctorOfficeResponseDTO createOffice(DoctorOfficeRequestDTO dto) {
        DoctorOffice office = new DoctorOffice();
        office.setOfficeName(dto.getOfficeName());
        office.setDoctorName(dto.getDoctorName());
        office.setSpecialty(dto.getSpecialty());
        office.setAddress(dto.getAddress());
        office.setPhone(dto.getPhone());
        office.setStoreId(dto.getStoreId());
        office.setRelationshipStage(dto.getRelationshipStage() != null ? dto.getRelationshipStage() : RelationshipStage.NEW);
        office.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.NORMAL);
        office.setOwnerId(dto.getOwnerId());

        return mapToDTO(repository.save(office));
    }

    @Override
    public DoctorOfficeResponseDTO updateOffice(UUID id, DoctorOfficeRequestDTO dto) {
        DoctorOffice office = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor office not found with id: " + id));

        office.setOfficeName(dto.getOfficeName());
        office.setDoctorName(dto.getDoctorName());
        office.setSpecialty(dto.getSpecialty());
        office.setAddress(dto.getAddress());
        office.setPhone(dto.getPhone());
        office.setStoreId(dto.getStoreId());
        if (dto.getRelationshipStage() != null) {
            office.setRelationshipStage(dto.getRelationshipStage());
        }
        if (dto.getPriority() != null) {
            office.setPriority(dto.getPriority());
        }
        office.setOwnerId(dto.getOwnerId());

        return mapToDTO(repository.save(office));
    }

    @Override
    public DoctorOfficeResponseDTO getOfficeById(UUID id) {
        DoctorOffice office = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor office not found with id: " + id));
        return mapToDTO(office);
    }

    @Override
    public List<DoctorOfficeResponseDTO> getAllOffices(RelationshipStage stage, UUID ownerId, UUID storeId, String specialty) {
        List<DoctorOffice> offices;

        if (stage != null) {
            offices = repository.findByRelationshipStage(stage);
        } else if (ownerId != null) {
            offices = repository.findByOwnerId(ownerId);
        } else if (storeId != null) {
            offices = repository.findByStoreId(storeId);
        } else if (specialty != null && !specialty.isBlank()) {
            offices = repository.findBySpecialtyIgnoreCase(specialty);
        } else {
            offices = repository.findAll();
        }

        return offices.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteOffice(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Doctor office not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private DoctorOfficeResponseDTO mapToDTO(DoctorOffice office) {
        DoctorOfficeResponseDTO dto = new DoctorOfficeResponseDTO();
        dto.setId(office.getId());
        dto.setOfficeName(office.getOfficeName());
        dto.setDoctorName(office.getDoctorName());
        dto.setSpecialty(office.getSpecialty());
        dto.setAddress(office.getAddress());
        dto.setPhone(office.getPhone());
        dto.setStoreId(office.getStoreId());
        dto.setRelationshipStage(office.getRelationshipStage());
        dto.setPriority(office.getPriority());
        dto.setOwnerId(office.getOwnerId());
        dto.setCreatedAt(office.getCreatedAt());
        dto.setUpdatedAt(office.getUpdatedAt());
        return dto;
    }
}