package io.trino.historyserver.service;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.fetch.TrinoQueryFetcher;
import io.trino.historyserver.storage.RetryingStorageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueryService
{
    private final TrinoQueryFetcher trinoQueryFetcher;
    private final RetryingStorageHandler storageHandler;

    public QueryService(TrinoQueryFetcher trinoQueryFetcher, RetryingStorageHandler storageHandler)
    {
        this.trinoQueryFetcher = trinoQueryFetcher;
        this.storageHandler = storageHandler;
    }

    public void createQuery(QueryReference queryRef, String environment)
    {
        String queryJson = trinoQueryFetcher.fetchQuery(queryRef);
        storageHandler.writeQuery(queryRef.queryId(), environment, queryJson);
    }

    public String getQuery(String queryId, String environment)
    {
        return storageHandler.readQuery(queryId, environment);
    }
}
