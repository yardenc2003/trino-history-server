package io.trino.historyserver.storage;

import io.trino.historyserver.exception.QueryStorageException;
import org.springframework.stereotype.Service;

@Service
public interface QueryStorageHandler {
    void storeQuery(String queryId, String queryJson)
            throws QueryStorageException;
    String readQuery(String queryId)
            throws QueryStorageException;
}
