package io.trino.historyserver.controller;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.service.controller.QueryImportService;
import io.trino.historyserver.util.QueryReferenceFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/import")
public class QueryImportController
{

    private static final Logger logger = LoggerFactory.getLogger(QueryImportController.class);

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

        logger.info("event=received_query_complete_event queryId={} coordinator={}",
                queryRef.queryId(),
                queryRef.coordinatorUrl());

        queryImportService.importQuery(queryRef);

        return String.format(
                "Query %s was successfully imported.",
                queryRef.queryId()
        );
    }
}
