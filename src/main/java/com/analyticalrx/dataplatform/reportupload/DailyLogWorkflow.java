package com.analyticalrx.dataplatform.reportupload;

import java.io.IOException;
import java.io.InputStream;

// Starter for https://github.com/analyticalrx/data-platform/issues/77.
// Register this class as a Spring bean when the Daily Log workflow is ready.
public final class DailyLogWorkflow implements ReportUploadWorkflow {

    @Override
    public String reportType() {
        return "DAILY_LOG"; // Match the report_type written by the upload API.
    }

    @Override
    public ReportUploadResult process(UploadReceipt receipt, InputStream report) throws IOException {
        // TODO Parse the PrimeRx CSV into Daily Log records. A record can span several lines.
        //
        // TODO Validate the layout and required fields before inserting any rows.
        // If the file identifies a pharmacy, check it against receipt.pharmacyKey().
        // For a bad layout, return:
        // return new ReportUploadResult.Rejected(ReportUploadResult.ErrorCode.INVALID_LAYOUT);
        //
        // TODO Apply the approved field map from issue #77. Use receipt.tenantId() and
        // receipt.pharmacyKey() for ownership, and receipt.uploadId() to link the imported rows.
        // Keep raw report values out of logs and errors.
        //
        // TODO Insert the approved rows in the transaction already started by the scheduler.
        // Return new ReportUploadResult.Processed(rowCount) with the number of rows written.
        // Let temporary read and database errors propagate so the scheduler can roll back and retry.
        //
        // The scheduler closes the stream and updates control_plane.upload_receipt.
        throw new UnsupportedOperationException("Daily Log workflow starter");
    }
}
