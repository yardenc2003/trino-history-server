package io.trino.historyserver.controller;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.service.QueryService;
import io.trino.historyserver.dto.QueryReferenceFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/query")
public class QueryController
{
    private final QueryService queryService;
    private final QueryReferenceFactory queryReferenceFactory;

    public QueryController(QueryService queryService, QueryReferenceFactory queryReferenceFactory)
    {
        this.queryService = queryService;
        this.queryReferenceFactory = queryReferenceFactory;
    }

    @PostMapping
    public String createQuery(@RequestBody String queryCompletedJson, HttpServletRequest request)
    {
        QueryReference queryRef = queryReferenceFactory.create(queryCompletedJson, request);

        log.info("event=received_query_complete_event queryId={} coordinator={}",
                queryRef.queryId(),
                queryRef.coordinatorUrl());

        queryService.createQuery(queryRef);
        log.info("event=create_query_succeeded queryId={}", queryRef.queryId());

        return String.format(
                "Query %s was successfully created.",
                queryRef.queryId()
        );
    }

    @GetMapping
    public ResponseEntity<Void> handleBaseQueryPath()
    {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{queryId}")
    public ResponseEntity<String> getQuery(@PathVariable String queryId)
    {
        log.info("event=received_query_read_event queryId={}", queryId);

        String queryJson = queryService.getQuery(queryId);
        log.info("event=get_query_succeeded queryId={}", queryId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(queryJson);
    }
}
