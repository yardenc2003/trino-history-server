package io.trino.historyserver.exception;

public class TrinoAuthException
        extends RuntimeException
{
    public TrinoAuthException(String message)
    {
        super(message);
    }

    public TrinoAuthException(String message, Throwable cause)
    {
        super(message, cause);
    }
}