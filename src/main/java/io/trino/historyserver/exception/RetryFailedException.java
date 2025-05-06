package io.trino.historyserver.exception;

public class RetryFailedException
        extends RuntimeException
{
    public RetryFailedException(String message)
    {
        super(message);
    }

    public RetryFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }
}