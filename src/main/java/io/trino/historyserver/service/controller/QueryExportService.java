package io.trino.historyserver.service.controller;

import io.trino.historyserver.service.storage.QueryStorageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueryExportService
{
    private final QueryStorageHandler queryStorageHandler;

    public QueryExportService(QueryStorageHandler queryStorageHandler)
    {
        this.queryStorageHandler = queryStorageHandler;
    }

    public String exportQuery(String queryId)
    {
        String queryJson = queryStorageHandler.readQuery(queryId);

        log.info("event=export_query_succeeded queryId={}", queryId);
        return queryJson;
    }
}
