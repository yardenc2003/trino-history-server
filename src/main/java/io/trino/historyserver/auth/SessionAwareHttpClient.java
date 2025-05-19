package io.trino.historyserver.auth;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.ExpiredSessionException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
public class SessionAwareHttpClient
{
    private final TrinoSessionManager sessionManager;
    private final WebClient webClient;

    public SessionAwareHttpClient(TrinoSessionManager sessionManager, WebClient webClient) {
        this.sessionManager = sessionManager;
        this.webClient = webClient;
    }

    public <T> T runWithSessionRetry(QueryReference queryRef, Function<WebClient, Mono<T>> requestLogic) {
        String cookie = sessionManager.getSessionCookie(queryRef.coordinatorUrl());

        try {
            return requestLogic.apply(webClientWithCookieHeader(cookie)).block();
        } catch (ExpiredSessionException e) {
            sessionManager.refreshSessionCookie(queryRef.coordinatorUrl());
            cookie = sessionManager.getSessionCookie(queryRef.coordinatorUrl());

            return requestLogic.apply(webClientWithCookieHeader(cookie)).block();
        }
    }

    private WebClient webClientWithCookieHeader(String cookie) {
        return webClient.mutate()
                .defaultHeader(HttpHeaders.COOKIE, cookie)
                .build();
    }
}
