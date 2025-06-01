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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "jdbc")
@RequiredArgsConstructor
public abstract class JdbcStorageHandler
        implements QueryStorageHandler
{
    protected static final String QUERIES_TABLE = "query_history";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    protected abstract List<String> initializeStatements();

    protected abstract String insertQueryStatement();

    protected abstract String selectQueryStatement();

    @PostConstruct
    private void initializeSchema()
    {
        for (String sql : initializeStatements()) {
            try {
                jdbcTemplate.execute(sql);
            }
            catch (DataAccessException e) {
                throw new StorageInitializationException(
                        String.format(
                                "Failed to execute schema initialization SQL: %s", sql
                        ), e
                );
            }
        }
        log.info("event=schema_initialization type=success");
    }

    @Override
    public void writeQuery(String queryId, String environment, String queryJson)
            throws QueryStorageException
    {
        String sql = insertQueryStatement();
        Map<String, Object> params = Map.of(
                "queryId", queryId,
                "environment", environment,
                "queryJson", queryJson
        );

        try {
            namedJdbcTemplate.update(sql, params);
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
        String sql = selectQueryStatement();
        Map<String, Object> params = Map.of(
                "queryId", queryId,
                "environment", environment
        );

        try {
            String queryJson = namedJdbcTemplate.queryForObject(sql, params, String.class);
            log.info("event=query_read_succeeded type=success queryId={} table=\"{}\"", queryId, QUERIES_TABLE);
            return queryJson;
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
    }
}
