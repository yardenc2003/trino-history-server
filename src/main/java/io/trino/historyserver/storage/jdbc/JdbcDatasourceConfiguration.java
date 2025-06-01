package io.trino.historyserver.storage.jdbc;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JdbcDatasourceConfiguration
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
