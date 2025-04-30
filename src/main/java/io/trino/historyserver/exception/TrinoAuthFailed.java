package io.trino.historyserver.exception;

public class TrinoAuthFailed
        extends RuntimeException
{
    public TrinoAuthFailed(String message)
    {
        super(message);
    }
}