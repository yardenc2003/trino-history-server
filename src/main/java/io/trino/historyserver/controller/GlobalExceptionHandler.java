package io.trino.historyserver.controller;

import io.trino.historyserver.exception.InvalidQueryEventException;
import io.trino.historyserver.exception.QueryFetchException;
import io.trino.historyserver.exception.QueryStorageException;
import io.trino.historyserver.exception.TrinoAuthFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidQueryEventException.class)
    public ResponseEntity<String> handleInvalidEventError(InvalidQueryEventException e) {
        logger.error("event=invalid_query_event type=client_error message=\"{}\"", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Received invalid query event: " + e.getMessage());
    }

    @ExceptionHandler(TrinoAuthFailed.class)
    public ResponseEntity<String> handleTrinoAuthError(TrinoAuthFailed e) {
        logger.error("event=trino_auth_failed type=server_error message=\"{}\"", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to authenticate with the coordinator: " + e.getMessage());
    }

    @ExceptionHandler(QueryFetchException.class)
    public ResponseEntity<String> handleFetchError(QueryFetchException e) {
        logger.error("event=query_fetch_failed type=server_error queryId={} message=\"{}\"", e.getQueryId(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch query from coordinator: " + e.getMessage());
    }

    @ExceptionHandler(QueryStorageException.class)
    public ResponseEntity<String> handleStorageError(QueryStorageException e) {
        logger.error("event=query_store_failed type=server_error queryId={} message=\"{}\"", e.getQueryId(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to store query: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericError(Exception e) {
        logger.error("event=unhandled_exception type=server_error message=\"{}\"", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong: " + e.getMessage());
    }
}
