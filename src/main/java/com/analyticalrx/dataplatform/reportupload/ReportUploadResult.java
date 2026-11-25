package com.analyticalrx.dataplatform.reportupload;

import java.util.Objects;

public sealed interface ReportUploadResult {

    record Processed(long rowCount) implements ReportUploadResult {
        public Processed {
            if (rowCount < 0) {
                throw new IllegalArgumentException("The report row count cannot be negative");
            }
        }
    }

    record Rejected(ErrorCode errorCode) implements ReportUploadResult {
        public Rejected {
            Objects.requireNonNull(errorCode, "A rejected report must have a safe error code");
        }
    }

    enum ErrorCode {
        INVALID_LAYOUT,
        INVALID_REPORT_DATA,
        PHARMACY_MISMATCH
    }
}
