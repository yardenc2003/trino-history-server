package io.trino.historyserver.exception;

public class QueryStorageException
        extends QueryException
{
    public QueryStorageException(String message, String queryId)
    {
        super(message, queryId);
    }

    public QueryStorageException(String message, String queryId, Throwable cause)
    {
        super(message, queryId, cause);
    }
}