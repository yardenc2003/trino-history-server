package io.trino.historyserver.service.storage;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.QueryStorageException;
import org.springframework.stereotype.Service;

@Service
public interface QueryStorageHandler {
    void storeQuery(QueryReference queryRef, String queryJson)
            throws QueryStorageException;
    String readFullQuery(QueryReference queryRef)
            throws QueryStorageException;
}
