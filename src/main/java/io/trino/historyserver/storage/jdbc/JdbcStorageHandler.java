package io.trino.historyserver.storage.jdbc;

import io.trino.historyserver.exception.QueryStorageException;
import io.trino.historyserver.exception.StorageInitializationException;
import io.trino.historyserver.storage.QueryStorageHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "jdbc")
@RequiredArgsConstructor
public abstract class JdbcStorageHandler
        implements QueryStorageHandler
{
    protected static final String QUERIES_TABLE = "query_history";

    private final JdbcTemplate jdbcTemplate;

    protected abstract String getInitializeTableStatement();

    protected abstract String getInsertQueryStatement();

    protected abstract String getSelectQueryStatement();

    @PostConstruct
    private void tableInitialization()
    {
        try {
            jdbcTemplate.execute(getInitializeTableStatement());
        }
        catch (DataAccessException e) {
            throw new StorageInitializationException(
                    String.format(
                            "Failed to create table \"%s\".",
                            QUERIES_TABLE
                    ), e
            );
        }
        log.info("event=table_create_succeeded type=success table=\"{}\"", QUERIES_TABLE);
    }

    @Override
    public void writeQuery(String queryId, String environment, String queryJson)
            throws QueryStorageException
    {
        try {
            jdbcTemplate.update(
                    getInsertQueryStatement(),
                    queryId,
                    environment,
                    queryJson
            );
        }
        catch (DuplicateKeyException e) {
            throw new QueryStorageException(
                    String.format(
                            "Query %s already exists in table \"%s\".",
                            queryId, QUERIES_TABLE
                    ),
                    queryId, e
            );
        }
        catch (DataAccessException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to write query %s to table \"%s\".",
                            queryId, QUERIES_TABLE
                    ),
                    queryId, e
            );
        }
        log.info("event=query_store_succeeded type=success queryId={} table=\"{}\"", queryId, QUERIES_TABLE);
    }

    @Override
    public String readQuery(String queryId, String environment)
            throws QueryStorageException
    {
        String queryJson;

        try {
            queryJson = jdbcTemplate.queryForObject(
                    getSelectQueryStatement(),
                    String.class,
                    queryId, environment
            );
        }
        catch (EmptyResultDataAccessException e) {
            throw new QueryStorageException(
                    String.format(
                            "Query %s not found in table \"%s\" (environment: \"%s\").",
                            queryId, QUERIES_TABLE, environment
                    ),
                    queryId, e
            );
        }
        catch (DataAccessException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to read query %s from table \"%s\".",
                            queryId, QUERIES_TABLE
                    ),
                    queryId, e
            );
        }
        log.info("event=query_read_succeeded type=success queryId={} table=\"{}\"", queryId, QUERIES_TABLE);
        return queryJson;
    }
}

