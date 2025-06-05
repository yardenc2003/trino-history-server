package io.trino.historyserver.storage.jdbc.dialects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "jdbc")
public interface SqlDialect
{
    // SQL for creating a simple query history with a query ID, textual environment name and the query information JSON
    List<String> initializeStatements();

    // SQL for inserting historical query values into the table
    String insertQueryStatement();

    // SQL for selecting query by ID and environment values combination
    String selectQueryStatement();
}
