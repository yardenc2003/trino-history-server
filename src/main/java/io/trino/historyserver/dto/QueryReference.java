package io.trino.historyserver.dto;

public record QueryReference(String queryId, String coordinatorUrl) {
    public QueryReference(String queryId) {
        this(queryId, null);
    }
}
