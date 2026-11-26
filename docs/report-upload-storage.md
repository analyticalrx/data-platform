# Report Upload Storage — Ticket #65

## Staging infrastructure

- Bucket: `analyticalrx-report-uploads-staging`
- Region: `us-east-1`
- Object prefix: `incoming/`
- Object key format: `incoming/<uploadId>`
- Public access: Blocked
- Encryption: SSE-S3 (AES256)
- Object ownership: BucketOwnerEnforced
- Transport security: HTTPS required

## Lifecycle configuration

- Rule: `archive-processed-reports`
- Prefix: `incoming/`
- Required object tag: `processed=true`
- Transition: S3 Glacier Flexible Retrieval
- Transition eligibility: 0 days after object creation
- Nonempty objects smaller than 128 KB are included.
- Lifecycle transitions are asynchronous.
- Glacier Flexible Retrieval has a 90-day minimum storage billing duration.

## Restore procedure

Only authorized operators should restore archived reports.

1. Verify the object's storage class using `aws s3api head-object`.
2. Request a temporary restore:

```bash
aws s3api restore-object \
  --bucket analyticalrx-report-uploads-staging \
  --key "incoming/$UPLOAD_ID" \
  --restore-request '{"Days":1,"GlacierJobParameters":{"Tier":"Standard"}}'
```

3. Check restore status:

```bash
aws s3api head-object \
  --bucket analyticalrx-report-uploads-staging \
  --key "incoming/$UPLOAD_ID" \
  --query Restore
```

4. Download the object only after the restore status indicates `ongoing-request="false"`.

Standard retrieval typically takes 3–5 hours. Retrieval charges may apply. The restored copy is temporary; the original object remains archived.

## Verification completed

- Bucket creation
- Public access protection
- Default encryption
- Object ownership
- HTTPS enforcement policy configuration
- Lifecycle configuration
- Synthetic upload and download using local AWS identity
- Object tagging

## Pending

- Runtime IAM permissions
- Approved retention and expiration policy
- Actual Glacier transition and restore verification
- Production storage provisioning
- Deployed runtime access verification
- Deployed multipart upload limit verification
