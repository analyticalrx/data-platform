package repository;

import entity.Task;
import entity.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndTenantId(Long id, Long tenantId);

    Page<Task> findByTenantId(Long tenantId, Pageable pageable);

    Page<Task> findByTenantIdAndStatus(
            Long tenantId,
            TaskStatus status,
            Pageable pageable
    );

    Page<Task> findByTenantIdAndPharmacyKey(
            Long tenantId,
            String pharmacyKey,
            Pageable pageable
    );

    Page<Task> findByTenantIdAndAssigneeId(
            Long tenantId,
            Long assigneeId,
            Pageable pageable
    );

    Optional<Task> findByIdAndTenantIdAndPharmacyKey(
            Long id,
            Long tenantId,
            String pharmacyKey
    );
}