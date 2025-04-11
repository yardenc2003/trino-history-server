package io.trino.historyserver.service.storage;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.QueryStorageException;

public interface QueryStorageHandler {
    void storePreviewQuery(QueryReference queryRef, String queryJson)
            throws QueryStorageException;
    void storeFullQuery(QueryReference queryRef, String queryJson)
            throws QueryStorageException;
}
