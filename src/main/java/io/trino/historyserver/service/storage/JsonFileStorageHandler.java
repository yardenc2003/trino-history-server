package io.trino.historyserver.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.QueryStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JsonFileStorageHandler
        implements QueryStorageHandler
{
    private static final String FILE_EXTENSION = ".json";

    @Value("${storage.queries-base-dir:./data}")
    private String baseDir;

    @Value("${storage.query-dir:query}")
    private String queryDir;

    @Override
    public void storeQuery(QueryReference queryRef, String queryJson)
            throws QueryStorageException
    {
        storeQuery(queryRef, queryJson, this::getQueryPath);
    }

    private void storeQuery(
            QueryReference queryRef,
            String queryJson,
            Function<QueryReference, Path> pathResolver
    )
            throws QueryStorageException
    {
        Path path = pathResolver.apply(queryRef);

        try {
            this.store(path, queryJson);
        }
        catch (IOException e) {
            throw new QueryStorageException(
                    String.format(
                            "Failed to write query %s JSON to file. path=%s reason=%s",
                            queryRef.queryId(), path, e.getMessage()
                    ),
                    queryRef.queryId()
            );
        }
        log.info("event=query_store_succeeded type=success queryId={} path=\"{}\"", queryRef.queryId(), path);
    }

    private void store(Path fullPath, String content)
            throws IOException
    {
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public Path getQueryPath(QueryReference queryRef)
    {
        return Path.of(baseDir, queryDir, queryRef.queryId() + FILE_EXTENSION);
    }
}
