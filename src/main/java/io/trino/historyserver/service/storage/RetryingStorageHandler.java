package io.trino.historyserver.service.storage;

import io.trino.historyserver.exception.QueryStorageException;
import io.trino.historyserver.util.TaskRetryExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetryingStorageHandler
        implements QueryStorageHandler
{
    @Value("${storage.connect-max-retries:3}")
    private int maxRetries;

    @Value("${storage.connect-backoff:500}")
    private long backoffMillis;

    private final QueryStorageHandler delegate;
    private final TaskRetryExecutor taskRetryExecutor;

    @Override
    public void storeQuery(String queryId, String queryJson)
            throws QueryStorageException
    {
        taskRetryExecutor.executeWithRetry(() -> delegate.storeQuery(queryId, queryJson), maxRetries, backoffMillis);
    }

    @Override
    public String readQuery(String queryId)
            throws QueryStorageException
    {
        return taskRetryExecutor.executeWithRetry(() -> delegate.readQuery(queryId), maxRetries, backoffMillis);
    }
}
