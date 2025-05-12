package io.trino.historyserver.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import io.trino.historyserver.exception.QueryStorageException;
import io.trino.historyserver.exception.StorageInitializationException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "file")
public class LocalFileSystemStorageHandler
        implements QueryStorageHandler
{
    private static final String FILE_EXTENSION = ".json";

    @Value("${storage.query-dir:query}")
    private String queryDir;

    @PostConstruct
    private void ensureDirectoryExists()
    {
        try {
            Files.createDirectories(Path.of(queryDir));
        }
        catch (IOException e) {
            throw new StorageInitializationException(
                    String.format(
                            "Failed to create directory \"%s\" existence due to filesystem error.",
                            queryDir
                    ), e
            );
        }
        log.info("event=directory_create_succeeded type=success path=\"{}\"", queryDir);

    }

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
                            "Failed to write query %s JSON to path \"%s\".",
                            queryId, path
                    ),
                    queryId, e
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
                            "Failed to read query %s JSON from path \"%s\".",
                            queryId, path
                    ),
                    queryId, e
            );
        }
        log.info("event=query_read_succeeded type=success queryId={} path=\"{}\"", queryId, path);
        return queryJson;
    }

    private void store(Path fullPath, String content)
            throws IOException
    {
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
