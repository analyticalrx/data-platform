package com.analyticalrx.dataplatform.reportupload;

import java.io.IOException;
import java.io.InputStream;

public interface ReportUploadWorkflow {

    String reportType();

    /**
     * Validate the report before writing any rows. Use a Spring-managed repository or
     * JdbcTemplate with the scheduler's DataSource and write on the calling thread.
     * Return the number of rows written. Let write failures propagate so the scheduler
     * can roll back the report rows and receipt update together. The scheduler closes
     * the input stream.
     */
    ReportUploadResult process(UploadReceipt receipt, InputStream report) throws IOException;
}
