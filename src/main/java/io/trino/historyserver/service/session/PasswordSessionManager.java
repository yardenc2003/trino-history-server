package io.trino.historyserver.service.session;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

import io.trino.historyserver.exception.TrinoAuthFailed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PasswordSessionManager
        implements TrinoSessionManager
{
    @Value("${trino-api.auth.username:}")
    private String username;

    @Value("${trino-api.auth.password:}")
    private String password;

    private final Map<String, String> sessionCookies = new ConcurrentHashMap<>();
    private final WebClient webClient;

    public PasswordSessionManager(WebClient webClient) {this.webClient = webClient;}

    @Override
    public String getSessionCookie(String coordinatorUrl)
    {
        return sessionCookies.computeIfAbsent(coordinatorUrl, this::fetchSessionCookie);
    }

    @Override
    public void refreshSessionCookie(String coordinatorUrl)
    {
        sessionCookies.compute(coordinatorUrl, (key, val) -> fetchSessionCookie(coordinatorUrl));
    }

    private String fetchSessionCookie(String coordinatorUrl)
    {
        String url = coordinatorUrl + "/ui/login";

        String cookie = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(createLoginForm())
                .exchangeToMono(response -> handleLoginResponse(response, coordinatorUrl))
                .block();

        log.info("event=trino_cookie_fetch_succeeded type=success coordinator={}", coordinatorUrl);
        return cookie;
    }

    private BodyInserters.FormInserter<String> createLoginForm()
    {
        return fromFormData("username", username)
                .with("password", password);
    }

    private Mono<String> handleLoginResponse(ClientResponse response, String coordinatorUrl)
    {
        if (response.statusCode().is3xxRedirection()) {
            return Mono.just(getSessionCookie(response, coordinatorUrl));
        }
        return Mono.error(loginFailedError(response, coordinatorUrl));
    }

    private String getSessionCookie(ClientResponse response, String coordinatorUrl)
    {
        List<String> cookies = response.headers().header(HttpHeaders.SET_COOKIE);
        return extractSessionCookie(cookies, coordinatorUrl);
    }

    private String extractSessionCookie(List<String> cookies, String coordinatorUrl)
    {
        return cookies.stream()
                .filter(cookie -> cookie.startsWith("Trino-UI-Token"))
                .findFirst()
                .map(cookie -> cookie.split(";")[0])
                .orElseThrow(() -> noSessionCookieError(coordinatorUrl));
    }

    private TrinoAuthFailed noSessionCookieError(String coordinatorUrl)
    {
        return new TrinoAuthFailed(
                String.format(
                        "The coordinator %s didn't send a session cookie.",
                        coordinatorUrl
                )
        );
    }

    private TrinoAuthFailed loginFailedError(ClientResponse response, String coordinatorUrl)
    {
        return new TrinoAuthFailed(
                String.format("Login to coordinator %s failed with status %s. cause=%s",
                        coordinatorUrl,
                        response.statusCode(),
                        response.bodyToMono(String.class)
                )
        );
    }
}