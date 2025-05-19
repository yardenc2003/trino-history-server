package io.trino.historyserver.fetch;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.ExpiredSessionException;
import io.trino.historyserver.exception.QueryFetchException;
import io.trino.historyserver.auth.SessionAwareHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrinoQueryFetcher
{
    public static final String TRINO_UI_QUERY_PATH = "/ui/api/query";

    private final SessionAwareHttpClient sessionAwareHttpClient;

    public String fetchQuery(QueryReference queryRef)
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

    private Mono<QueryFetchException> createQueryFetchException(
            ClientResponse response,
            String baseMessage,
            QueryReference queryRef
    )
    {
        if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
            return Mono.error(new ExpiredSessionException("Coordinator auth cookie expired"));
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