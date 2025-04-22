package io.trino.historyserver.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils
{
    private final ObjectMapper objectMapper;

    public JsonUtils(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public String toJson(Object object)
    {
        return objectMapper.writeValueAsString(object);
    }
}