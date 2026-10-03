package com.primerx.task.repository;

import com.primerx.task.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndTenantId(Long id, String tenantId);

    Page<Task> findByTenantId(String tenantId, Pageable pageable);
}