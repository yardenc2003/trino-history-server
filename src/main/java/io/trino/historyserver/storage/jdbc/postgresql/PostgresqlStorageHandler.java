package io.trino.historyserver.storage.jdbc.postgresql;

import io.trino.historyserver.storage.jdbc.JdbcStorageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.jdbc.impl", havingValue = "postgresql")
public class PostgresqlStorageHandler
        extends JdbcStorageHandler
{
    public PostgresqlStorageHandler(JdbcTemplate jdbcTemplate)
    {
        super(jdbcTemplate);
    }

    @Override
    protected String getInitializeTableStatement()
    {
        return String.format("""
                    CREATE TABLE IF NOT EXISTS %s (
                        query_id VARCHAR(50) PRIMARY KEY,
                        environment VARCHAR(50) NOT NULL,
                        query_info JSONB NOT NULL
                    )
                """, QUERIES_TABLE
        );
    }

    @Override
    protected String getInsertQueryStatement()
    {
        return String.format(
                "INSERT INTO %s (query_id, environment, query_info) VALUES (?, ?, ?::jsonb)",
                QUERIES_TABLE
        );
    }

    @Override
    protected String getSelectQueryStatement()
    {
        return String.format(
                "SELECT query_info FROM %s WHERE query_id = ? AND environment = ?",
                QUERIES_TABLE
        );
    }
}
