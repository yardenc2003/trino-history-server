package io.trino.historyserver.exception;

public class InvalidQueryEventException
        extends RuntimeException
{
    public InvalidQueryEventException(String message)
    {
        super(message);
    }
}
