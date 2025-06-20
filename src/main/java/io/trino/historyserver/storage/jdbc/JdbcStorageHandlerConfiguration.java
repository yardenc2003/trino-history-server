package io.trino.historyserver.storage.jdbc;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "storage.type", havingValue = "jdbc")
public class JdbcStorageHandlerConfiguration
{
    @Bean
    @ConfigurationProperties("storage.jdbc")
    public DataSourceProperties dataSourceProperties()
    {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource dataSource()
    {
        return dataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }
}
