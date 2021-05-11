package io.hosuaby;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class BugVertxTest {
  static WireMockServer wireMock = new WireMockServer(options().dynamicPort());
  static int serverPort;
  static HttpClient http11;
  static HttpClient http2;

  @BeforeAll
  @DisplayName("Setup WireMock server")
  static void setupWireMockServer() {
    wireMock.start();
    serverPort = wireMock.port();
    wireMock.stubFor(get(anyUrl())
        .willReturn(aResponse()
            .withStatus(200)));
  }

  @AfterAll
  @DisplayName("Shutdown WireMock server")
  static void shutdownWireMockServer() {
    wireMock.stop();
  }

  @BeforeAll
  @DisplayName("Create HTTP1.1 Vertx server")
  static void createHttp11Client(Vertx vertx) {
    HttpClientOptions options = new HttpClientOptions();
    http11 = vertx.createHttpClient(options);
  }

  @BeforeAll
  @DisplayName("Create HTTP2 Vertx server")
  static void createHttp2Client(Vertx vertx) {
    HttpClientOptions options = new HttpClientOptions()
        .setProtocolVersion(HttpVersion.HTTP_2);
    http2 = vertx.createHttpClient(options);
  }

  @Test
  @DisplayName("Request with HTTP1.1 client should succeed")
  public void testHttp11RequestShouldSucceed(VertxTestContext testContext) {
    WebClient
        .wrap(http11)
        .request(HttpMethod.GET, serverPort, "localhost", "/")
        .send()
        .onComplete(response -> {
          if (response.succeeded()) {
            testContext.completeNow();
          } else {
            testContext.failNow(response.cause());
          }
        });
  }

  @Test
  @DisplayName("Request with HTTP2 client should succeed")
  public void testHttp2RequestShouldSucceed(VertxTestContext testContext) {
    WebClient
        .wrap(http2)
        .request(HttpMethod.GET, serverPort, "localhost", "/")
        .send()
        .onComplete(response -> {
          if (response.succeeded()) {
            testContext.completeNow();
          } else {
            testContext.failNow(response.cause());
          }
        });
  }

  @Nested
  @DisplayName("After server has became unavailable")
  class AfterServerBecameUnavailable {

    @BeforeEach
    void shutDownServer() {
      wireMock.stop();
    }

    @Test
    @DisplayName("Request with HTTP1.1 client should fail")
    public void testHttp11RequestShouldFail(VertxTestContext testContext) {
      WebClient
          .wrap(http11)
          .request(HttpMethod.GET, serverPort, "localhost", "/")
          .send()
          .onComplete(response -> {
            if (response.succeeded()) {
              testContext.failNow("Client should not get responses from unavailable server");
            } else {
              testContext.completeNow();
            }
          });
    }

    @Test
    @DisplayName("Request with HTTP2 client should fail")
    public void testHttp2RequestShouldFail(VertxTestContext testContext) {
      WebClient
          .wrap(http2)
          .request(HttpMethod.GET, serverPort, "localhost", "/")
          .send()
          .onComplete(response -> {
            if (response.succeeded()) {
              testContext.failNow("Client should not get responses from unavailable server");
            } else {
              testContext.completeNow();
            }
          });
    }

    @Nested
    @DisplayName("After server is available again")
    class AfterServerIsBack {
      WireMockServer restartedServer = new WireMockServer(options().port(serverPort));

      @BeforeEach
      void restartServer() {
        restartedServer.start();
        restartedServer.stubFor(get(anyUrl())
            .willReturn(aResponse()
                .withStatus(200)));
      }

      @AfterEach
      void shutDownServer() {
        restartedServer.stop();
      }

      @Test
      @DisplayName("Request with HTTP1.1 client should succeed")
      public void testHttp11RequestShouldSucceed(VertxTestContext testContext) {
        WebClient
            .wrap(http11)
            .request(HttpMethod.GET, serverPort, "localhost", "/")
            .send()
            .onComplete(response -> {
              if (response.succeeded()) {
                testContext.completeNow();
              } else {
                testContext.failNow(response.cause());
              }
            });
      }

      @Test
      @DisplayName("Request with HTTP2 client should succeed")
      public void testHttp2RequestShouldSucceed(VertxTestContext testContext) {
        WebClient
            .wrap(http2)
            .request(HttpMethod.GET, serverPort, "localhost", "/")
            .send()
            .onComplete(response -> {
              if (response.succeeded()) {
                testContext.completeNow();
              } else {
                testContext.failNow(response.cause());
              }
            });
      }
    }
  }
}
