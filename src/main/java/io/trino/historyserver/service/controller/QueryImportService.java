package io.trino.historyserver.service.controller;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.service.fetch.QueryFetcher;
import io.trino.historyserver.service.storage.QueryStorageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueryImportService
{

    private final QueryFetcher queryFetcher;
    private final QueryStorageHandler queryStorageHandler;

    public QueryImportService(QueryFetcher queryFetcher, QueryStorageHandler queryStorageHandler)
    {
        this.queryFetcher = queryFetcher;
        this.queryStorageHandler = queryStorageHandler;
    }

    public void importQuery(QueryReference queryRef)
    {
        String queryJson = queryFetcher.fetchQuery(queryRef);
        queryStorageHandler.storeQuery(queryRef, queryJson);

        log.info("event=import_query_succeeded queryId={}", queryRef.queryId());
    }
}
