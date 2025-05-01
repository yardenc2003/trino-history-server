package io.trino.historyserver.controller;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.service.controller.QueryExportService;
import io.trino.historyserver.util.QueryReferenceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ui/api/query")
public class QueryExportController
{
    private final QueryExportService queryExportService;
    private final QueryReferenceFactory queryReferenceFactory;

    public QueryExportController(QueryExportService queryExportService, QueryReferenceFactory queryReferenceFactory)
    {
        this.queryExportService = queryExportService;
        this.queryReferenceFactory = queryReferenceFactory;
    }

    @GetMapping
    public ResponseEntity<Void> handleBaseQueryPath() {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{queryId}")
    public ResponseEntity<String> getQueryJson(@PathVariable String queryId)
    {
        log.info("event=received_query_read_event queryId={}", queryId);
        QueryReference queryRef = queryReferenceFactory.create(queryId);

        String queryJson = queryExportService.exportQuery(queryRef);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(queryJson);
    }
}
