package io.trino.historyserver.dto;

import lombok.NonNull;

public record QueryReference(@NonNull String queryId, @NonNull String coordinatorUrl) {}
