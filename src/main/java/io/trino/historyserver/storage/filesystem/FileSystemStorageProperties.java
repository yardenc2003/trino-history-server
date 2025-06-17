package io.trino.historyserver.storage.filesystem;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "storage.filesystem")
public class FileSystemStorageProperties
{
    private String queryDir = "app/data/queries";
}
