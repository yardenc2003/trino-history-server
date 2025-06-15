package io.trino.historyserver.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@Validated
@ConfigurationProperties(prefix = "trino.auth")
public class TrinoAuthProperties
{
    @NotBlank(message = "trino.auth.username must be provided")
    private String username;
    @NotBlank(message = "trino.auth.password must be provided")
    private String password;
}