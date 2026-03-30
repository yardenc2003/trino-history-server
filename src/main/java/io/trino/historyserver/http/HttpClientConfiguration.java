package io.trino.historyserver.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "http-client")
public class HttpClientConfiguration
{
    @Bean
    public WebClient webClient(HttpClientProperties props)
    {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize((int) props.getMaxInMemorySize().toBytes()))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
