package io.trino.historyserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.InvalidQueryEventException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import static io.trino.historyserver.util.HttpUtils.COORDINATOR_CUSTOM_HEADER;

@Component
public class QueryReferenceFactory {

    private final ObjectMapper objectMapper;

    public QueryReferenceFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public QueryReference create(String queryCompletedJson, HttpServletRequest request) {
        String queryId = extractQueryId(queryCompletedJson);
        String coordinatorUrl = extractCoordinatorUrl(request);
        return new QueryReference(queryId, coordinatorUrl);
    }

    private String extractQueryId(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String queryId = root.at("/metadata/queryId").asText();
            if (queryId.isBlank()) {
                throw new InvalidQueryEventException("Missing 'queryId' field");
            }
            return queryId;
        } catch (JsonProcessingException e) {
            throw new InvalidQueryEventException("Malformed JSON");
        }
    }

    private String extractCoordinatorUrl(HttpServletRequest request) {
        String url = request.getHeader(COORDINATOR_CUSTOM_HEADER);
        if (url == null) {
            throw new InvalidQueryEventException(
                    String.format("Missing %s header", COORDINATOR_CUSTOM_HEADER)
            );
        }
        return url;
    }
}
