package io.vertx.starter;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private Vertx vertx;
  private int port;

  @Before
  public void setUp(TestContext context) throws IOException {
    vertx = Vertx.vertx();
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject().put("http.port", 8080));
    vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());
  }


  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testThatTheServerIsStarted(TestContext tc) {
    Async async = tc.async();
    vertx.createHttpClient()
      .post(8080, "localhost", "/ivr/dummy")
      .putHeader("content-length", "0")
      .handler(response -> {
          tc.assertEquals(response.statusCode(), 200);
          response.bodyHandler(body -> {
            tc.assertTrue(body.length() > 0);
            async.complete();
          });
        })
    .write("")
    .end();
  }
}
