package com.analyticalrx.dataplatform.reportupload;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

final class UploadReceiptRepository {

    private final JdbcTemplate jdbc;

    UploadReceiptRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    Optional<UploadReceipt> lockNextSavedReceipt() {
        // The scheduler keeps this transaction open until the worker and receipt update finish.
        String sql = """
                select upload_id, tenant_id, pharmacy_key, report_type, object_key
                from control_plane.upload_receipt
                where status = 'SAVED'
                  and (updated_at = created_at or updated_at <= current_timestamp - interval '10 seconds')
                order by updated_at, upload_id
                limit 1
                for update skip locked
                """;

        List<UploadReceipt> receipts = jdbc.query(sql, (row, rowNumber) -> new UploadReceipt(
                row.getLong("upload_id"),
                row.getLong("tenant_id"),
                row.getString("pharmacy_key"),
                row.getString("report_type"),
                row.getString("object_key")
        ));

        if (receipts.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(receipts.getFirst());
    }

    void markProcessed(long uploadId, long rowCount) {
        String sql = """
                update control_plane.upload_receipt
                set status = 'PROCESSED', committed_row_count = ?, safe_error = null, updated_at = clock_timestamp()
                where upload_id = ? and status = 'SAVED'
                """;
        int updatedRows = jdbc.update(sql, rowCount, uploadId);
        if (updatedRows != 1) {
            throw new IllegalStateException("Could not mark saved upload " + uploadId + " as processed");
        }
    }

    void markFailed(long uploadId, ReportUploadResult.ErrorCode errorCode) {
        String sql = """
                update control_plane.upload_receipt
                set status = 'FAILED', committed_row_count = null, safe_error = ?, updated_at = clock_timestamp()
                where upload_id = ? and status = 'SAVED'
                """;
        int updatedRows = jdbc.update(sql, errorCode.name(), uploadId);
        if (updatedRows != 1) {
            throw new IllegalStateException("Could not mark saved upload " + uploadId + " as failed");
        }
    }

    void deferRetry(long uploadId) {
        String sql = """
                update control_plane.upload_receipt
                set updated_at = clock_timestamp()
                where upload_id = ? and status = 'SAVED'
                """;
        jdbc.update(sql, uploadId);
    }
}
