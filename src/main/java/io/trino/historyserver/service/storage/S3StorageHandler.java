package io.trino.historyserver.service.storage;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.QueryStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class S3StorageHandler
        implements QueryStorageHandler
{
    private static final String FILE_EXTENSION = ".json";

    private final S3Client s3Client;

    @Value("${storage.s3.bucket:history}")
    private String bucketName;

    @Value("${storage.query-dir:query}")
    private String queryDir;

    @Override
    public void storeQuery(QueryReference queryRef, String queryJson)
            throws QueryStorageException
    {
        ensureBucketExists();
        String key = generateQueryKey(queryRef);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/json")
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromString(queryJson));
        }
        catch (S3Exception e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to write query %s JSON to path \"%s\" (bucket: %s), reason: %s",
                            queryRef.queryId(), key, bucketName, e.toString()
                    ),
                    queryRef.queryId()
            );
        }
    }

    @Override
    public String readQuery(QueryReference queryRef)
            throws QueryStorageException
    {
        String queryJson;
        String key = generateQueryKey(queryRef);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            queryJson = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (S3Exception | IOException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to read query %s JSON from path \"%s\" (bucket: %s), reason: %s",
                            queryRef.queryId(), key, bucketName, e.toString()
                    ),
                    queryRef.queryId()
            );
        }
        log.info("event=query_read_succeeded type=success queryId={} path=\"{}\"", queryRef.queryId(), key);
        return queryJson;
    }

    private void ensureBucketExists()
    {
        try {
            s3Client.headBucket(request -> request.bucket(bucketName));
        }
        catch (NoSuchBucketException exception) {
            s3Client.createBucket(request -> request.bucket(bucketName));
        }
    }

    public String generateQueryKey(QueryReference queryRef)
    {
        return Path.of(queryDir, queryRef.queryId() + FILE_EXTENSION).toString();
    }
}


