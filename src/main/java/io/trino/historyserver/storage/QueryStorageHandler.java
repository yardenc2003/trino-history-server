package io.trino.historyserver.storage;

import io.trino.historyserver.exception.QueryStorageException;
import org.springframework.stereotype.Service;

@Service
public interface QueryStorageHandler {
    void writeQuery(String queryId, String environment, String queryJson)
            throws QueryStorageException;
    String readQuery(String queryId, String environment)
            throws QueryStorageException;
}
