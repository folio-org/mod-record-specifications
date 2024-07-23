package org.folio.api;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import java.nio.file.Path;
import java.util.List;
import lombok.SneakyThrows;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.testing.type.IntegrationTest;
import org.folio.tenant.domain.dto.Parameter;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@IntegrationTest
class RecordSpecificationsAppIT {

  private static final Network NETWORK = Network.newNetwork();
  private static final Logger LOG = LoggerFactory.getLogger(RecordSpecificationsAppIT.class);

  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
    new PostgreSQLContainer<>("postgres:16-alpine")
      .withNetwork(NETWORK)
      .withNetworkAliases("mypostgres")
      .withExposedPorts(5432)
      .withUsername("username")
      .withPassword("password")
      .withDatabaseName("postgres");

  @Container
  private static final KafkaContainer KAFKA =
    new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.5.3"))
      .withNetwork(NETWORK)
      .withNetworkAliases("mykafka")
      .withExposedPorts(9093, 29092);

  @Container
  private static final GenericContainer<?> MOD_RSPEC =
    new GenericContainer<>(
      new ImageFromDockerfile("mod-record-specifications").withFileFromPath(".", Path.of("../")))
      .withNetwork(NETWORK)
      .withExposedPorts(8081)
      .withAccessToHost(true)
      .dependsOn(POSTGRES, KAFKA)
      .withEnv("DB_HOST", "mypostgres")
      .withEnv("DB_PORT", "5432")
      .withEnv("DB_USERNAME", "username")
      .withEnv("DB_PASSWORD", "password")
      .withEnv("DB_DATABASE", "postgres")
      .withEnv("KAFKA_HOST", "mykafka")
      .withEnv("KAFKA_PORT", "9092");

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeAll
  static void beforeAll() {
    RestAssured.reset();
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    RestAssured.baseURI = "http://" + MOD_RSPEC.getHost() + ":" + MOD_RSPEC.getFirstMappedPort();
    MOD_RSPEC.followOutput(new Slf4jLogConsumer(LOG).withSeparateOutputStreams().withPrefix("rspec"));
  }

  @BeforeEach
  void beforeEach() {
    RestAssured.requestSpecification = null;
  }

  @Test
  @DisplayName("Health endpoint should work without X-Okapi-Tenant")
  void health() {
    when()
      .get("/admin/health")
      .then()
      .statusCode(200)
      .body("status", is("UP"));
  }

  @Test
  @DisplayName("POST tenant should work idempotent")
  void installAndUpgrade() {
    setTenant("latest");

    TenantAttributes body = new TenantAttributes()
      .moduleTo("999999.0.0")
      .parameters(List.of(new Parameter("loadReference").value("true")));

    postTenant(body);

    body.moduleFrom("0.0.0");
    postTenant(body);

    given()
      .when()
      .get("/specification-storage/specifications")
      .then()
      .statusCode(200)
      .body("specifications.size()", is(2));
  }

  @Test
  @DisplayName("Logging should be in expected format")
  void canLog() {
    setTenant("logtenant");

    given()
      .header(XOkapiHeaders.REQUEST_ID, "987654321")
      .header(XOkapiHeaders.USER_ID, "9532bfb9-0887-4445-a0e7-c02efd68f790")
      .when()
      .get("/specification-storage/specifications")
      .then()
      .statusCode(500);  // tenant hasn't been created

    assertThat(MOD_RSPEC.getLogs(), containsString(
      "[987654321] [logtenant] [9532bfb9-0887-4445-a0e7-c02efd68f790] [mod-record-specifications]"));
  }

  private void setTenant(String tenant) {
    RestAssured.requestSpecification = new RequestSpecBuilder()
      .addHeader(XOkapiHeaders.TENANT, tenant)
      .setContentType(ContentType.JSON)
      .build();
  }

  @SneakyThrows
  private void postTenant(TenantAttributes body) {
    given()
      .body(mapper.writeValueAsString(body))
      .when()
      .post("/_/tenant")
      .then()
      .statusCode(204)
      .log().all(true);
  }

}
