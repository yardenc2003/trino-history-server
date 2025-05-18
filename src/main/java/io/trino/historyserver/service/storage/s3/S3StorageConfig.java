package io.trino.historyserver.service.storage.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.StorageClass;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageConfig
{
    @Value("${storage.s3.region}")
    private String region;

    @Value("${storage.s3.endpoint}")
    private String endpoint;

    @Value("${storage.s3.storage-class:STANDARD}")
    private String storageClass;

    @Value("${storage.s3.path-style-access:true}")
    private boolean pathStyleAccess;

    @Value("${storage.s3.accessKey}")
    private String accessKey;

    @Value("${storage.s3.secretKey}")
    private String secretKey;

    @Bean
    public StorageClass s3StorageClass()
    {
        try {
            return Enum.valueOf(StorageClass.class, storageClass.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid S3 storage class: " + storageClass, e);
        }
    }

    @Bean
    public S3Client s3Client()
    {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(pathStyleAccess)
                                .build()
                )
                .build();
    }
}
