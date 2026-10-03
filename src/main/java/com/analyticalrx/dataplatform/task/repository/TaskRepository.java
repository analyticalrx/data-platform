package com.analyticalrx.dataplatform.task.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.analyticalrx.dataplatform.auth.access.TenantSchemaResolver;
import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.task.model.CreateTaskRequest;
import com.analyticalrx.dataplatform.task.model.TaskCommentResponse;
import com.analyticalrx.dataplatform.task.model.TaskPriority;
import com.analyticalrx.dataplatform.task.model.TaskResponse;
import com.analyticalrx.dataplatform.task.model.TaskStatus;

@Repository
public class TaskRepository {
    private static final RowMapper<TaskResponse> TASK_MAPPER = (rs, rowNum) -> new TaskResponse(
            rs.getLong("id"), rs.getString("pharmacy_key"), rs.getString("title"),
            rs.getString("description"), TaskStatus.valueOf(rs.getString("status")),
            TaskPriority.valueOf(rs.getString("priority")), rs.getObject("due_at", OffsetDateTime.class),
            rs.getLong("assignee_user_id"), rs.getLong("created_by_user_id"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class),
            rs.getObject("completed_at", OffsetDateTime.class));

    private static final RowMapper<TaskCommentResponse> COMMENT_MAPPER = (rs, rowNum) -> new TaskCommentResponse(
            rs.getLong("id"), rs.getLong("task_id"), rs.getLong("author_user_id"),
            rs.getString("comment_text"), rs.getObject("created_at", OffsetDateTime.class));

    private final NamedParameterJdbcTemplate jdbc;
    private final TenantSchemaResolver tenantSchemaResolver;

    public TaskRepository(NamedParameterJdbcTemplate jdbc, TenantSchemaResolver tenantSchemaResolver) {
        this.jdbc = jdbc;
        this.tenantSchemaResolver = tenantSchemaResolver;
    }

    public boolean canAssignTo(long assigneeUserId, long tenantId, String pharmacyKey) {
        String sql = """
                select exists (
                  select 1 from control_plane.app_user u
                  join control_plane.user_role ur on ur.user_id = u.id and ur.status = 'active'
                  join control_plane.role_definition r on r.id = ur.role_id and r.status = 'active'
                  join control_plane.role_permission rp on rp.role_id = r.id
                  join control_plane.permission_definition pd on pd.id = rp.permission_id
                    and pd.permission_code = 'task.read' and pd.status = 'active'
                  where u.id = :assigneeId and u.tenant_id = :tenantId and u.status = 'active'
                    and (r.role_code = 'OWNER' or exists (
                      select 1 from control_plane.user_pharmacy_access pa
                      where pa.user_id = u.id and pa.tenant_id = u.tenant_id
                        and pa.pharmacy_key = :pharmacyKey and pa.status = 'active'
                    ))
                )
                """;
        var params = new MapSqlParameterSource()
                .addValue("assigneeId", assigneeUserId)
                .addValue("tenantId", tenantId)
                .addValue("pharmacyKey", pharmacyKey);
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, params, Boolean.class));
    }

    public long create(PlatformUserDetails user, CreateTaskRequest request) {
        String schema = tenantSchemaResolver.findActiveSchemaName(user);
        String sql = """
                insert into %s.task (tenant_id, pharmacy_key, title, description, priority,
                                     due_at, assignee_user_id, created_by_user_id)
                values (:tenantId, :pharmacyKey, :title, :description, :priority,
                        :dueAt, :assigneeId, :creatorId)
                returning id
                """.formatted(schema);
        var params = new MapSqlParameterSource()
                .addValue("tenantId", user.tenantId())
                .addValue("pharmacyKey", request.pharmacyKey())
                .addValue("title", request.title().trim())
                .addValue("description", request.description())
                .addValue("priority", request.priority() == null ? TaskPriority.MEDIUM.name() : request.priority().name())
                .addValue("dueAt", request.dueAt())
                .addValue("assigneeId", request.assigneeUserId())
                .addValue("creatorId", user.id());
        return jdbc.queryForObject(sql, params, Long.class);
    }

    public List<TaskResponse> listVisible(PlatformUserDetails user, String pharmacyKey, TaskStatus status) {
        var params = scopeParams(user);
        StringBuilder sql = new StringBuilder(scopedSelect(user));
        if (pharmacyKey != null) {
            sql.append(" and t.pharmacy_key = :pharmacyKey");
            params.addValue("pharmacyKey", pharmacyKey);
        }
        if (status != null) {
            sql.append(" and t.status = :status");
            params.addValue("status", status.name());
        }
        sql.append(" order by t.due_at asc nulls last, t.created_at desc limit 100");
        return jdbc.query(sql.toString(), params, TASK_MAPPER);
    }

    public Optional<TaskResponse> findVisibleById(PlatformUserDetails user, long taskId, boolean lock) {
        var params = scopeParams(user).addValue("taskId", taskId);
        String sql = scopedSelect(user) + " and t.id = :taskId" + (lock ? " for update of t" : "");
        return jdbc.query(sql, params, TASK_MAPPER).stream().findFirst();
    }

    public void updateStatus(PlatformUserDetails user, long taskId, TaskStatus status) {
        String schema = tenantSchemaResolver.findActiveSchemaName(user);
        String sql = """
                update %s.task
                set status = :status, updated_at = now(),
                    completed_at = case when :status = 'COMPLETED' then now() else completed_at end
                where id = :taskId and tenant_id = :tenantId
                """.formatted(schema);
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("status", status.name())
                .addValue("taskId", taskId)
                .addValue("tenantId", user.tenantId()));
    }

    public TaskCommentResponse addComment(PlatformUserDetails user, long taskId, String text) {
        String schema = tenantSchemaResolver.findActiveSchemaName(user);
        String sql = """
                insert into %s.task_comment (tenant_id, task_id, author_user_id, comment_text)
                values (:tenantId, :taskId, :authorId, :commentText)
                returning id, task_id, author_user_id, comment_text, created_at
                """.formatted(schema);
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("tenantId", user.tenantId())
                .addValue("taskId", taskId)
                .addValue("authorId", user.id())
                .addValue("commentText", text.trim()), COMMENT_MAPPER);
    }

    public List<TaskCommentResponse> listComments(PlatformUserDetails user, long taskId) {
        String schema = tenantSchemaResolver.findActiveSchemaName(user);
        String sql = """
                select id, task_id, author_user_id, comment_text, created_at
                from %s.task_comment
                where tenant_id = :tenantId and task_id = :taskId
                order by created_at asc, id asc
                """.formatted(schema);
        return jdbc.query(sql, new MapSqlParameterSource()
                .addValue("tenantId", user.tenantId())
                .addValue("taskId", taskId), COMMENT_MAPPER);
    }

    public void recordActivity(PlatformUserDetails user, long taskId, String type,
                               TaskStatus oldStatus, TaskStatus newStatus) {
        String schema = tenantSchemaResolver.findActiveSchemaName(user);
        String sql = """
                insert into %s.task_activity
                    (tenant_id, task_id, actor_user_id, activity_type, old_status, new_status)
                values (:tenantId, :taskId, :actorId, :activityType, :oldStatus, :newStatus)
                """.formatted(schema);
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("tenantId", user.tenantId())
                .addValue("taskId", taskId)
                .addValue("actorId", user.id())
                .addValue("activityType", type)
                .addValue("oldStatus", oldStatus == null ? null : oldStatus.name())
                .addValue("newStatus", newStatus == null ? null : newStatus.name()));
    }

    private MapSqlParameterSource scopeParams(PlatformUserDetails user) {
        return new MapSqlParameterSource()
                .addValue("tenantId", user.tenantId())
                .addValue("userId", user.id());
    }

    private String scopedSelect(PlatformUserDetails user) {
        String schema = tenantSchemaResolver.findActiveSchemaName(user);
        String sql = """
                select t.id, t.pharmacy_key, t.title, t.description, t.status, t.priority,
                       t.due_at, t.assignee_user_id, t.created_by_user_id,
                       t.created_at, t.updated_at, t.completed_at
                from %s.task t
                join %s.pharmacy p on p.tenant_id = t.tenant_id
                  and p.pharmacy_key = t.pharmacy_key and p.status = 'active'
                where t.tenant_id = :tenantId
                """.formatted(schema, schema);

        if (!user.roles().contains("OWNER")) {
            sql += """
                     and exists (
                       select 1 from control_plane.user_pharmacy_access pa
                       where pa.user_id = :userId and pa.tenant_id = t.tenant_id
                         and pa.pharmacy_key = t.pharmacy_key and pa.status = 'active'
                     )
                    """;
        }
        if (!user.roles().contains("OWNER") && !user.roles().contains("STORE_MANAGER")) {
            sql += " and (t.assignee_user_id = :userId or t.created_by_user_id = :userId)";
        }
        return sql;
    }
}
