package com.analyticalrx.dataplatform.crm.interaction;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctor-offices/{officeId}/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    @Autowired
    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping
    public ResponseEntity<InteractionResponseDTO> recordInteraction(
            @PathVariable UUID officeId,
            @Valid @RequestBody InteractionRequestDTO requestDTO) {
        return new ResponseEntity<>(interactionService.recordInteraction(officeId, requestDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<InteractionResponseDTO>> getInteractionsByOffice(@PathVariable UUID officeId) {
        return ResponseEntity.ok(interactionService.getInteractionsByOffice(officeId));
    }
}