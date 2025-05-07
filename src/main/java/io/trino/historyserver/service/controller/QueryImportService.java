package io.trino.historyserver.service.controller;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.service.fetch.QueryFetcher;
import io.trino.historyserver.service.storage.RetryingStorageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueryImportService
{

    private final QueryFetcher queryFetcher;
    private final RetryingStorageHandler storageHandler;

    public QueryImportService(QueryFetcher queryFetcher, RetryingStorageHandler storageHandler)
    {
        this.queryFetcher = queryFetcher;
        this.storageHandler = storageHandler;
    }

    public void importQuery(QueryReference queryRef)
    {
        String queryJson = queryFetcher.fetchQuery(queryRef);
        storageHandler.storeQuery(queryRef.queryId(), queryJson);
    }
}
