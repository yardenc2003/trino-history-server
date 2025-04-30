package io.trino.historyserver.exception;

public class QueryFetchException
        extends QueryException
{
    public QueryFetchException(String message, String queryId)
    {
        super(message, queryId);
    }
}