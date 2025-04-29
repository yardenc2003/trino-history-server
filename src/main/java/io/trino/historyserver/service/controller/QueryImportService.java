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
        fetchAndStore(queryRef, true);
        fetchAndStore(queryRef, false);
    }

    private void fetchAndStore(QueryReference queryRef, boolean isPreview)
    {
        String type = isPreview ? "preview" : "full";

        String queryJson = isPreview
                ? queryFetcher.fetchPreviewQuery(queryRef)
                : queryFetcher.fetchFullQuery(queryRef);

        if (isPreview) {
            queryStorageHandler.storePreviewQuery(queryRef, queryJson);
        }
        else {
            queryStorageHandler.storeFullQuery(queryRef, queryJson);
        }

        log.info("event=import_{}_query_succeeded queryId={}", type, queryRef.queryId());
    }
}
