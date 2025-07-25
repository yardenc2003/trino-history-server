package io.trino.historyserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class TaskRetryExecutor
{
    public <T> T executeWithRetry(Supplier<T> task, int maxRetries, long backoffMillis)
    {
        RuntimeException lastException = new RuntimeException();

        for (int i = 1; i <= maxRetries; i++) {
            try {
                return task.get();
            }
            catch (RuntimeException e) {
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
        log.warn("event=task_failed type=server_error message=\"All task {} retries failed\"" , maxRetries);
        throw lastException;
    }

    public void executeWithRetry(Runnable task, int maxRetries, long backoffMillis)
    {
        executeWithRetry(() -> {
            task.run();
            return null;
        }, maxRetries, backoffMillis);
    }
}
