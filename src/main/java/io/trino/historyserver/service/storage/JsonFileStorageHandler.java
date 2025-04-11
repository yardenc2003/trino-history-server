package io.trino.historyserver.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

import io.trino.historyserver.dto.QueryReference;
import io.trino.historyserver.exception.QueryStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JsonFileStorageHandler
        implements QueryStorageHandler
{

    @Value("${storage.queries-base-dir:./data}")
    private String baseDir;

    @Value("${storage.preview-query-dir:preview}")
    private String previewQueryDir;

    @Value("${storage.full-query-dir:full}")
    private String fullQueryDir;

    @Override
    public void storePreviewQuery(QueryReference queryRef, String queryJson)
            throws QueryStorageException
    {
        storeQuery(queryRef, queryJson, this::getPreviewQueryPath);
    }

    @Override
    public void storeFullQuery(QueryReference queryRef, String queryJson)
            throws QueryStorageException
    {
        storeQuery(queryRef, queryJson, this::getFullQueryDirQueryPath);
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
    }

    private void store(Path fullPath, String content)
            throws IOException
    {
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public Path getPreviewQueryPath(QueryReference queryRef)
    {
        return Path.of(baseDir, previewQueryDir, queryRef.queryId() + ".json");
    }

    public Path getFullQueryDirQueryPath(QueryReference queryRef)
    {
        return Path.of(baseDir, fullQueryDir, queryRef.queryId() + ".json");
    }
}
