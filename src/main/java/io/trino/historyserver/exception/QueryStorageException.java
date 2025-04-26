package io.trino.historyserver.exception;

public class QueryStorageException
        extends QueryException
{
    public QueryStorageException(String message, String queryId)
    {
        super(message, queryId);
    }
}