package io.trino.historyserver.storage.jdbc.dialects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "storage.jdbc.dialect", havingValue = "postgresql")
public class PostgresqlDialect
        implements SqlDialect
{
    @Override
    public List<String> initializeStatements()
    {
        String createTable = """
            CREATE TABLE IF NOT EXISTS query_history (
                query_id VARCHAR(255) PRIMARY KEY,
                environment VARCHAR(255) NOT NULL,
                query_info JSONB NOT NULL
            )
            """;

        String createIndex = """
            CREATE INDEX IF NOT EXISTS idx_environment_query_id
            ON query_history (environment, query_id)
            """;

        return List.of(createTable, createIndex);
    }

    @Override
    public String insertQueryStatement()
    {
        return """
            INSERT INTO query_history (query_id, environment, query_info)
            VALUES (:queryId, :environment, :queryJson::jsonb)
            """;
    }

    @Override
    public String selectQueryStatement()
    {
        return """
            SELECT query_info FROM query_history
            WHERE query_id = :queryId AND environment = :environment
            """;
    }
}
