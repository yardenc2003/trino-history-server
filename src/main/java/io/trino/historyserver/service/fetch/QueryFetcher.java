package io.trino.historyserver.service.fetch;

import java.util.List;
import java.util.Map;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.ExpiredSessionException;
import io.trino.historyserver.exception.QueryFetchException;
import io.trino.historyserver.service.client.SessionAwareHttpClient;
import io.trino.historyserver.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import static io.trino.historyserver.util.HttpUtils.TRINO_UI_QUERY_PATH;

@Slf4j
@Service
public class QueryFetcher
{
    private final SessionAwareHttpClient sessionAwareHttpClient;
    private final JsonUtils jsonUtils;

    public QueryFetcher(SessionAwareHttpClient sessionAwareHttpClient, JsonUtils jsonUtils)
    {
        this.sessionAwareHttpClient = sessionAwareHttpClient;
        this.jsonUtils = jsonUtils;
    }

    public String fetchPreviewQuery(QueryReference queryRef)
            throws QueryFetchException
    {
        List<Map<String, Object>> queryList = fetchAllPreviewQueries(queryRef);

        Map<String, Object> referredQuery = queryList.stream()
                .filter(query ->
                        queryRef.queryId().equals(query.get("queryId"))
                )
                .findFirst()
                .orElseThrow(() -> queryNotFoundError(queryRef));

        return jsonUtils.toJson(referredQuery);
    }

    public String fetchFullQuery(QueryReference queryRef)
            throws QueryFetchException
    {
        String url = queryRef.coordinatorUrl() + TRINO_UI_QUERY_PATH + "/" + queryRef.queryId();
        String baseMessage = String.format(
                "Error while fetching query %s data from coordinator %s.",
                queryRef.queryId(),
                queryRef.coordinatorUrl()
        );

        String queryJson = sessionAwareHttpClient.runWithSessionRetry(queryRef,
                client -> client.get()
                        .uri(url)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError,
                                response -> createQueryFetchException(
                                        response,
                                        baseMessage,
                                        queryRef
                                )
                        )
                        .bodyToMono(String.class)
        );

        log.info("event=query_fetch_succeeded type=success queryId={} coordinator={}", queryRef.queryId(), queryRef.coordinatorUrl());
        return queryJson;
    }

    private List<Map<String, Object>> fetchAllPreviewQueries(QueryReference queryRef)
            throws QueryFetchException
    {
        String url = queryRef.coordinatorUrl() + TRINO_UI_QUERY_PATH;
        String baseMessage = String.format(
                "Error while fetching queries preview data from coordinator %s.",
                queryRef.coordinatorUrl()
        );

        List<Map<String, Object>> queries = sessionAwareHttpClient.runWithSessionRetry(queryRef,
                client ->client.get()
                        .uri(url)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError,
                                response -> createQueryFetchException(
                                        response,
                                        baseMessage,
                                        queryRef
                                )
                        )
                        .bodyToMono(new ParameterizedTypeReference<>() {})
        );

        log.info("event=queries_fetch_succeeded type=success queryId={} coordinator={}", queryRef.queryId(), queryRef.coordinatorUrl());
        return queries;
    }

    private QueryFetchException queryNotFoundError(QueryReference queryRef)
    {
        return new QueryFetchException(
                String.format(
                        "Query %s was not found at coordinator %s",
                        queryRef.queryId(),
                        queryRef.coordinatorUrl()
                ),
                queryRef.queryId()
        );
    }

    private Mono<QueryFetchException> createQueryFetchException(
            ClientResponse response,
            String baseMessage,
            QueryReference queryRef
    )
    {
        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return Mono.error(new ExpiredSessionException("Coordinator session cookie expired"));
        }

        return response.bodyToMono(String.class)
                .defaultIfEmpty(baseMessage)
                .map(msg -> new QueryFetchException(
                                String.format("%s cause=%s", baseMessage, msg),
                                queryRef.queryId()
                        )
                );
    }
}