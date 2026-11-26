package com.analyticalrx.dataplatform.reportupload;

public record UploadReceipt(long uploadId, long tenantId, String pharmacyKey, String reportType, String objectKey) {
}
