package io.trino.historyserver.service;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.fetch.QueryFetcher;
import io.trino.historyserver.storage.RetryingStorageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueryService
{
    private final QueryFetcher queryFetcher;
    private final RetryingStorageHandler storageHandler;

    public QueryService(QueryFetcher queryFetcher, RetryingStorageHandler storageHandler)
    {
        this.queryFetcher = queryFetcher;
        this.storageHandler = storageHandler;
    }

    public void createQuery(QueryReference queryRef)
    {
        String queryJson = queryFetcher.fetchQuery(queryRef);
        storageHandler.storeQuery(queryRef.queryId(), queryJson);
    }

    public String getQuery(String queryId)
    {
        String queryJson = storageHandler.readQuery(queryId);

        return queryJson;
    }
}
