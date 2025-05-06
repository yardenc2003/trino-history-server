package io.trino.historyserver.util;

import io.trino.historyserver.exception.RetryFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class RetryExecutor
{
    public <T> T executeWithRetry(Supplier<T> task, int maxRetries, long backoffMillis)
    {
        Exception lastException = null;

        for (int i = 1; i <= maxRetries; i++) {
            try {
                return task.get();
            }
            catch (Exception e) {
                lastException = e;
                log.warn("event=task_retry_failed type=server_error message=\"retry {}/{} failed due to {}\"", i, maxRetries, e.getMessage());
                if (i < maxRetries) {
                    try {
                        Thread.sleep(backoffMillis * i);
                    }
                    catch (InterruptedException ignored) {
                    }
                }
            }
        }
        throw new RetryFailedException(
                "All task retries failed",
                lastException
        );
    }

    public void executeWithRetry(Runnable task, int maxRetries, long backoffMillis)
    {
        executeWithRetry(() -> {
            task.run();
            return null;
        }, maxRetries, backoffMillis);
    }
}
