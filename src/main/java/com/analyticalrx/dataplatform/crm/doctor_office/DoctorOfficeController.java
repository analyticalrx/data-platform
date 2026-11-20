package com.analyticalrx.dataplatform.crm.doctor_office;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctor-offices")
public class DoctorOfficeController {

    private final DoctorOfficeService officeService;

    @Autowired
    public DoctorOfficeController(DoctorOfficeService officeService) {
        this.officeService = officeService;
    }

    @PostMapping
    public ResponseEntity<DoctorOfficeResponseDTO> createOffice(@Valid @RequestBody DoctorOfficeRequestDTO requestDTO) {
        return new ResponseEntity<>(officeService.createOffice(requestDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorOfficeResponseDTO> updateOffice(
            @PathVariable UUID id,
            @Valid @RequestBody DoctorOfficeRequestDTO requestDTO) {
        return ResponseEntity.ok(officeService.updateOffice(id, requestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorOfficeResponseDTO> getOfficeById(@PathVariable UUID id) {
        return ResponseEntity.ok(officeService.getOfficeById(id));
    }

    @GetMapping
    public ResponseEntity<List<DoctorOfficeResponseDTO>> getAllOffices(
            @RequestParam(required = false) RelationshipStage stage,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) String specialty) {
        return ResponseEntity.ok(officeService.getAllOffices(stage, ownerId, storeId, specialty));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffice(@PathVariable UUID id) {
        officeService.deleteOffice(id);
        return ResponseEntity.noContent().build();
    }
}