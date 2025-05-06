package io.trino.historyserver.exception;

public class QueryFetchException
        extends QueryException
{
    public QueryFetchException(String message, String queryId)
    {
        super(message, queryId);
    }

    public QueryFetchException(String message, String queryId, Throwable cause)
    {
        super(message, queryId, cause);
    }
}