package io.trino.historyserver.util;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class HttpUtils
{
    public static final String TRINO_UI_QUERY_PATH = "/ui/api/query";
    public static final String TRINO_UI_LOGIN_PATH = "/ui/login";
    public static final String TRINO_UI_COOKIE = "Trino-UI-Token";
    public static final String COORDINATOR_CUSTOM_HEADER = "X-Trino-Coordinator-Url";
    public static final String JSON_MEDIA_TYPE = "application/json";
}