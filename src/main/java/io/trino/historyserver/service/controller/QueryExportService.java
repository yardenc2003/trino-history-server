package io.trino.historyserver.service.controller;

import io.trino.historyserver.service.storage.RetryingStorageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueryExportService
{
    private final RetryingStorageHandler storageHandler;

    public QueryExportService(RetryingStorageHandler storageHandler)
    {
        this.storageHandler = storageHandler;
    }

    public String exportQuery(String queryId)
    {
        String queryJson = storageHandler.readQuery(queryId);

        log.info("event=export_query_succeeded queryId={}", queryId);
        return queryJson;
    }
}
