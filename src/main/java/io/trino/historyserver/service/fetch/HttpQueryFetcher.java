package io.trino.historyserver.service.fetch;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.ExpiredSessionException;
import io.trino.historyserver.exception.QueryFetchException;
import io.trino.historyserver.service.auth.PasswordSessionManager;
import io.trino.historyserver.service.auth.TrinoSessionManager;
import io.trino.historyserver.util.JsonUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class HttpQueryFetcher
        implements QueryFetcher
{
    private final TrinoSessionManager sessionManager;
    private final WebClient webClient;
    private final JsonUtils jsonUtils;

    public HttpQueryFetcher(PasswordSessionManager sessionManager, WebClient webClient, JsonUtils jsonUtils)
    {
        this.sessionManager = sessionManager;
        this.webClient = webClient;
        this.jsonUtils = jsonUtils;
    }

    @Override
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

    @Override
    public String fetchFullQuery(QueryReference queryRef)
            throws QueryFetchException
    {
        String url = queryRef.coordinatorUrl() + "/ui/api/query/" + queryRef.queryId();
        String baseMessage = String.format(
                "Error while fetching query %s data from coordinator %s.",
                queryRef.queryId(),
                queryRef.coordinatorUrl()
        );

        return runWithSessionRetry(queryRef,
                cookie ->
                        webClient.get()
                                .uri(url)
                                .header(HttpHeaders.COOKIE, cookie)
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
    }

    private List<Map<String, Object>> fetchAllPreviewQueries(QueryReference queryRef)
            throws QueryFetchException
    {
        String url = queryRef.coordinatorUrl() + "/ui/api/query";
        String baseMessage = String.format(
                "Error while fetching queries preview data from coordinator %s.",
                queryRef.coordinatorUrl()
        );

        return runWithSessionRetry(queryRef, cookie ->
                webClient.get()
                        .uri(url)
                        .header(HttpHeaders.COOKIE, cookie)
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
    }

    private <T> T runWithSessionRetry(QueryReference queryRef, Function<String, Mono<T>> requestLogic)
    {
        String cookie = sessionManager.getSessionCookie(queryRef.coordinatorUrl());

        try {
            return requestLogic.apply(cookie).block();
        }
        catch (ExpiredSessionException e) {
            sessionManager.refreshSessionCookie(queryRef.coordinatorUrl());
            cookie = sessionManager.getSessionCookie(queryRef.coordinatorUrl());

            return requestLogic.apply(cookie).block();
        }
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
            return Mono.error(new ExpiredSessionException());
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