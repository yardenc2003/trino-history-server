package io.trino.historyserver.storage.jdbc.postgresql;

import io.trino.historyserver.storage.jdbc.JdbcStorageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.jdbc.impl", havingValue = "postgresql")
public class PostgresqlStorageHandler
        extends JdbcStorageHandler
{
    public PostgresqlStorageHandler(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbcTemplate)
    {
        super(jdbcTemplate, namedJdbcTemplate);
    }

    @Override
    protected List<String> initializeStatements()
    {
        String createTable = String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    query_id VARCHAR(255) PRIMARY KEY,
                    environment VARCHAR(255) NOT NULL,
                    query_info JSONB NOT NULL
                )
                """, QUERIES_TABLE
        );

        String createIndex = String.format("""
                CREATE INDEX IF NOT EXISTS idx_environment_query_id
                ON %s (environment, query_id)
                """, QUERIES_TABLE
        );

        return List.of(createTable, createIndex);
    }

    @Override
    protected String insertQueryStatement()
    {
        return String.format("""
                INSERT INTO %s (query_id, environment, query_info)
                VALUES (:queryId, :environment, :queryJson::jsonb)
                """, QUERIES_TABLE
        );
    }

    @Override
    protected String selectQueryStatement()
    {
        return String.format("""
                SELECT query_info FROM %s
                WHERE query_id = :queryId AND environment = :environment"
                """, QUERIES_TABLE
        );
    }
}
