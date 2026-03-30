package io.trino.historyserver.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "http-client")
public class HttpClientProperties
{
    private DataSize maxInMemorySize = DataSize.parse("16MB");
}
