package com.analyticalrx.dataplatform.crm.interaction;

import com.analyticalrx.dataplatform.crm.doctor_office.DoctorOfficeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepository repository;
    private final DoctorOfficeRepository officeRepository;

    @Autowired
    public InteractionServiceImpl(InteractionRepository repository, DoctorOfficeRepository officeRepository) {
        this.repository = repository;
        this.officeRepository = officeRepository;
    }

    @Override
    public InteractionResponseDTO recordInteraction(UUID officeId, InteractionRequestDTO dto) {
        if (!officeRepository.existsById(officeId)) {
            throw new RuntimeException("Doctor office not found with id: " + officeId);
        }

        Interaction interaction = new Interaction();
        interaction.setOfficeId(officeId);
        interaction.setOccurredAt(dto.getOccurredAt());
        interaction.setVisitorId(dto.getVisitorId());
        interaction.setInteractionType(dto.getInteractionType());
        interaction.setPurpose(dto.getPurpose());
        interaction.setPitchId(dto.getPitchId());
        interaction.setPitchSnapshot(dto.getPitchSnapshot());
        interaction.setObjections(dto.getObjections());
        interaction.setOutcome(dto.getOutcome());
        interaction.setNotes(dto.getNotes());

        return mapToDTO(repository.save(interaction));
    }

    @Override
    public List<InteractionResponseDTO> getInteractionsByOffice(UUID officeId) {
        if (!officeRepository.existsById(officeId)) {
            throw new RuntimeException("Doctor office not found with id: " + officeId);
        }
        return repository.findByOfficeIdOrderByOccurredAtDesc(officeId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InteractionResponseDTO getInteractionById(UUID id) {
        Interaction interaction = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interaction record not found with id: " + id));
        return mapToDTO(interaction);
    }

    private InteractionResponseDTO mapToDTO(Interaction interaction) {
        InteractionResponseDTO dto = new InteractionResponseDTO();
        dto.setId(interaction.getId());
        dto.setOfficeId(interaction.getOfficeId());
        dto.setOccurredAt(interaction.getOccurredAt());
        dto.setVisitorId(interaction.getVisitorId());
        dto.setInteractionType(interaction.getInteractionType());
        dto.setPurpose(interaction.getPurpose());
        dto.setPitchId(interaction.getPitchId());
        dto.setPitchSnapshot(interaction.getPitchSnapshot());
        dto.setObjections(interaction.getObjections());
        dto.setOutcome(interaction.getOutcome());
        dto.setNotes(interaction.getNotes());
        dto.setCreatedAt(interaction.getCreatedAt());
        return dto;
    }
}