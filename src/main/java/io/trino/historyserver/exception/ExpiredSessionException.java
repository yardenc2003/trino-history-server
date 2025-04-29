package io.trino.historyserver.exception;

public class ExpiredSessionException
        extends RuntimeException
{
    public ExpiredSessionException(String message)
    {
        super(message);
    }
}