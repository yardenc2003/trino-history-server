package io.trino.historyserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
// Disable Spring Boot's automatic DataSource configuration
// because our app conditionally uses either JDBC or filesystem storage.
// This prevents startup failures when no JDBC datasource is configured.
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class HistoryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HistoryServerApplication.class, args);
	}
}
