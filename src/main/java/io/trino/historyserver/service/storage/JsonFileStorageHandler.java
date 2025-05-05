package io.trino.historyserver.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import io.trino.historyserver.exception.QueryStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "file")
public class JsonFileStorageHandler
        implements QueryStorageHandler
{
    private static final String FILE_EXTENSION = ".json";

    @Value("${storage.query-dir:query}")
    private String queryDir;

    @Override
    public void storeQuery(String queryId, String queryJson)
            throws QueryStorageException
    {
        Path path = getQueryPath(queryId);

        try {
            this.store(path, queryJson);
        }
        catch (IOException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to write query %s JSON to path \"%s\", reason: %s",
                            queryId, path, e
                    ),
                    queryId
            );
        }
        log.info("event=query_store_succeeded type=success queryId={} path=\"{}\"", queryId, path);
    }

    @Override
    public String readQuery(String queryId)
            throws QueryStorageException
    {
        String queryJson;
        Path path = getQueryPath(queryId);

        try {
            queryJson = this.read(path);
        }
        catch (IOException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to read query %s JSON from path \"%s\", reason: %s",
                            queryId, path, e
                    ),
                    queryId
            );
        }
        log.info("event=query_read_succeeded type=success queryId={} path=\"{}\"", queryId, path);
        return queryJson;
    }

    private void store(Path fullPath, String content)
            throws IOException
    {
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String read(Path fullPath)
            throws IOException
    {
        return Files.readString(fullPath);
    }

    public Path getQueryPath(String queryId)
    {
        return Path.of(queryDir, queryId + FILE_EXTENSION);
    }
}
