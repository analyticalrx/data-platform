package com.analyticalrx.dataplatform.crm.interaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
    List<Interaction> findByOfficeIdOrderByOccurredAtDesc(UUID officeId);
    List<Interaction> findByVisitorId(UUID visitorId);
    List<Interaction> findByPitchId(UUID pitchId);
}