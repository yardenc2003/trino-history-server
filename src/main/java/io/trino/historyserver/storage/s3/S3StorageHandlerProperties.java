package io.trino.historyserver.storage.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.model.StorageClass;

@Getter
@Setter
@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
@ConfigurationProperties(prefix = "storage.s3")
public class S3StorageHandlerProperties
{
    private String region;

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucket;

    private String queryDir;

    private StorageClass storageClass = StorageClass.STANDARD;

    private boolean pathStyleAccess = true;
}
