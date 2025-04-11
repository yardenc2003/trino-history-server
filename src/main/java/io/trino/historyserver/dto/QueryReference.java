package io.trino.historyserver.dto;

public record QueryReference(String queryId, String coordinatorUrl) {}
