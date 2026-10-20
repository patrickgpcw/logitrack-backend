package com.logitrack;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.jayway.jsonpath.JsonPath;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.datasource.url=jdbc:tc:postgresql:17.6-alpine:///logitrack_test",
		"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
		"spring.datasource.username=test",
		"spring.datasource.password=test"
})
class LogiTrackApplicationTests {

	@LocalServerPort
	int port;

	@Autowired
	JdbcTemplate jdbc;

	@Autowired
	Flyway flyway;

	@Test
	void appliesInitialMigrationToPostgres() {
		assertThat(jdbc.queryForObject(
				"SELECT count(*) FROM information_schema.schemata WHERE schema_name = 'logitrack'",
				Integer.class)).isEqualTo(1);
		assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
		assertThat(flyway.info().pending()).isEmpty();
		flyway.validate();
	}

	@Test
	void exposesHealthWithoutDatabaseDetails() throws Exception {
		try (var client = HttpClient.newHttpClient()) {
			var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/health"))
					.timeout(Duration.ofSeconds(10)).GET().build();
			var response = client.send(request, HttpResponse.BodyHandlers.ofString());

			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(JsonPath.parse(response.body()).read("$.status", String.class)).isEqualTo("UP");
			assertThat(response.body()).doesNotContain("\"components\"", "\"details\"", "jdbc:");
		}
	}
}
