package com.analyticalrx.dataplatform.crm.reporting;

import com.analyticalrx.dataplatform.crm.interaction.Pitch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PitchRepository extends JpaRepository<Pitch, UUID> {
    List<Pitch> findByActiveTrue();
}