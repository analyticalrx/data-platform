package com.analyticalrx.dataplatform.reportupload;

import java.io.IOException;
import java.io.InputStream;

public interface ReportUploadWorkflow {

    String reportType();

    /**
     * Validate the report before writing any rows. Use the current PostgreSQL transaction
     * so the report rows and receipt outcome commit together. Throw if processing should
     * be retried. The scheduler closes the input stream.
     */
    ReportUploadResult process(UploadReceipt receipt, InputStream report) throws IOException;
}
