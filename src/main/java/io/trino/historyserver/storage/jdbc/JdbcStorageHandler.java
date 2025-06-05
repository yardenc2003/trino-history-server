package io.trino.historyserver.storage.jdbc;

import io.trino.historyserver.exception.QueryStorageException;
import io.trino.historyserver.exception.StorageInitializationException;
import io.trino.historyserver.storage.QueryStorageHandler;
import io.trino.historyserver.storage.jdbc.dialects.SqlDialect;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "jdbc")
@RequiredArgsConstructor
public class JdbcStorageHandler
        implements QueryStorageHandler
{
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final SqlDialect dialect;

    @PostConstruct
    private void initializeSchema()
    {
        for (String sql : dialect.initializeStatements()) {
            try {
                namedJdbcTemplate.getJdbcTemplate().execute(sql);
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
        String sql = dialect.insertQueryStatement();
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
                            "Query %s already exists in query history table.",
                            queryId
                    ),
                    queryId, e
            );
        }
        catch (DataAccessException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to write query %s to query history table.",
                            queryId
                    ),
                    queryId, e
            );
        }
        log.info("event=query_record_write_succeeded type=success queryId={}", queryId);
    }

    @Override
    public String readQuery(String queryId, String environment)
            throws QueryStorageException
    {
        String sql = dialect.selectQueryStatement();
        Map<String, Object> params = Map.of(
                "queryId", queryId,
                "environment", environment
        );

        try {
            String queryJson = namedJdbcTemplate.queryForObject(sql, params, String.class);
            log.info("event=query_record_read_succeeded type=success queryId={}", queryId);
            return queryJson;
        }
        catch (EmptyResultDataAccessException e) {
            throw new QueryStorageException(
                    String.format(
                            "Query %s not found in query history table (environment: \"%s\").",
                            queryId, environment
                    ),
                    queryId, e
            );
        }
        catch (DataAccessException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to read query %s from query history table.",
                            queryId
                    ),
                    queryId, e
            );
        }
    }
}
