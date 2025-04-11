package io.trino.historyserver.service.fetch;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.QueryFetchException;

public interface QueryFetcher
{
    String fetchPreviewQuery(QueryReference queryRef)
            throws QueryFetchException;

    String fetchFullQuery(QueryReference queryRef)
            throws QueryFetchException;
}
