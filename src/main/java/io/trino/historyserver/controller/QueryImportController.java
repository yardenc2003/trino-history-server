package io.trino.historyserver.controller;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.service.controller.QueryImportService;
import io.trino.historyserver.util.QueryReferenceFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/import")
public class QueryImportController
{
    private final QueryImportService queryImportService;
    private final QueryReferenceFactory queryReferenceFactory;

    public QueryImportController(QueryImportService queryImportService, QueryReferenceFactory queryReferenceFactory)
    {
        this.queryImportService = queryImportService;
        this.queryReferenceFactory = queryReferenceFactory;
    }

    @PostMapping("/query-complete")
    public String completeQueryNotify(@RequestBody String queryCompletedJson, HttpServletRequest request)
    {
        QueryReference queryRef = queryReferenceFactory.create(queryCompletedJson, request);

        log.info("event=received_query_complete_event queryId={} coordinator={}",
                queryRef.queryId(),
                queryRef.coordinatorUrl());

        queryImportService.importQuery(queryRef);

        return String.format(
                "Query %s was successfully imported.",
                queryRef.queryId()
        );
    }
}
