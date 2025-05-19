package io.trino.historyserver.storage;

import io.trino.historyserver.exception.QueryStorageException;
import io.trino.historyserver.util.TaskRetryExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetryingStorageHandler
        implements QueryStorageHandler
{
    private final StorageRetryProperties props;
    private final QueryStorageHandler delegate;
    private final TaskRetryExecutor taskRetryExecutor;

    @Override
    public void writeQuery(String queryId, String queryJson)
            throws QueryStorageException
    {
        taskRetryExecutor.executeWithRetry(() -> delegate.writeQuery(queryId, queryJson), props.getMaxRetries(), props.getBackoffMillis());
    }

    @Override
    public String readQuery(String queryId)
            throws QueryStorageException
    {
        return taskRetryExecutor.executeWithRetry(() -> delegate.readQuery(queryId), props.getMaxRetries(), props.getBackoffMillis());
    }
}
