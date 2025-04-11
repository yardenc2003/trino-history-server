package io.trino.historyserver.exception;

import lombok.Getter;

@Getter
public class QueryException
        extends RuntimeException
{
    private final String queryId;

    public QueryException(String message, String queryId)
    {
        super(message);
        this.queryId = queryId;
    }
}