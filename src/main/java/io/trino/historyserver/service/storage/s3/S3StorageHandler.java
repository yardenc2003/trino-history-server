package io.trino.historyserver.service.storage.s3;

import io.trino.historyserver.exception.QueryStorageException;
import io.trino.historyserver.exception.StorageInitializationException;
import io.trino.historyserver.service.storage.QueryStorageHandler;
import io.trino.historyserver.util.HttpUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
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
    private final S3StorageProperties props;


    @PostConstruct
    private void ensureBucketExists()
    {
        try {
            s3Client.headBucket(request -> request.bucket(props.getBucket()));
        }
        catch (NoSuchBucketException e) {
            log.warn("event=bucket_does_not_exist type=warning bucket=\"{}\"", props.getBucket());
            createBucketIfNotExists();
        }
        catch (SdkException e) {
            throw new StorageInitializationException(
                    String.format(
                            "Failed to check bucket \"%s\" existence due to S3 error.",
                            props.getBucket()
                    ), e
            );
        }
    }

    @Override
    public void storeQuery(String queryId, String queryJson)
            throws QueryStorageException
    {
        String key = generateQueryKey(queryId);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(HttpUtils.JSON_MEDIA_TYPE)
                .storageClass(props.getStorageClass())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromString(queryJson));
        }
        catch (S3Exception e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to write query %s JSON to key \"%s\" (bucket: \"%s\")",
                            queryId, key, props.getBucket()
                    ),
                    queryId, e
            );
        }
        log.info("event=query_store_succeeded type=success queryId={} key=\"{}\" bucket=\"{}\"", queryId, key, props.getBucket());
    }

    @Override
    public String readQuery(String queryId)
            throws QueryStorageException
    {
        String queryJson;
        String key = generateQueryKey(queryId);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            queryJson = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (SdkException | IOException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to read query %s JSON from key \"%s\" (bucket: \"%s\")",
                            queryId, key, props.getBucket()
                    ),
                    queryId, e
            );
        }
        log.info("event=query_read_succeeded type=success queryId={} key=\"{}\" bucket=\"{}\"", queryId, key, props.getBucket());
        return queryJson;
    }

    private void createBucketIfNotExists()
    {
        try {
            s3Client.createBucket(request -> request.bucket(props.getBucket()));
        }
        catch (BucketAlreadyOwnedByYouException e) {
            log.warn("event=bucket_create_skipped type=warning bucket=\"{}\"", props.getBucket());
            return;
        }
        catch (SdkException e) {
            throw new StorageInitializationException(
                    String.format(
                            "Failed to create bucket \"%s\" due to S3 error.",
                            props.getBucket()
                    ), e
            );
        }
        log.info("event=bucket_create_succeeded type=success bucket=\"{}\"", props.getBucket());
    }

    private String generateQueryKey(String queryId)
    {
        return Path.of(props.getQueryDir(), queryId + FILE_EXTENSION).toString();
    }
}


