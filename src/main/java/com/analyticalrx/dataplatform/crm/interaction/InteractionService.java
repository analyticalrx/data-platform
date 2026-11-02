package com.analyticalrx.dataplatform.crm.interaction;

import java.util.List;
import java.util.UUID;

public interface InteractionService {
    InteractionResponseDTO recordInteraction(UUID officeId, InteractionRequestDTO requestDTO);
    List<InteractionResponseDTO> getInteractionsByOffice(UUID officeId);
    InteractionResponseDTO getInteractionById(UUID id);
}