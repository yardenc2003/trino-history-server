package io.trino.historyserver.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "trino.auth")
public class TrinoAuthProperties
{
    private String username;
    private String password;
}