package io.trino.historyserver.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "storage")
public class RetryingStorageHandlerProperties
{
    private int maxRetries = 3;
    private long backoffMillis = 500;
}