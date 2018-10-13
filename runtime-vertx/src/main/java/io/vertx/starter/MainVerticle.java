package io.vertx.starter;

import com.experiment.ivr.core.core.exception.ApplicationNotFoundException;
import com.experiment.ivr.core.core.exception.SessionNotFoundException;
import com.experiment.ivr.usecase.ContinueCall;
import com.experiment.ivr.usecase.StartCall;
import com.experiment.ivr.usecase.impl.ContinueCallImpl;
import com.experiment.ivr.usecase.impl.StartCallImpl;
import com.experiment.ivr.usecase.model.Request;
import com.experiment.ivr.usecase.model.Response;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.flogger.Flogger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Flogger
public class MainVerticle extends AbstractVerticle {

  private StartCall newCall = new StartCallImpl();

  private ContinueCall existingCall = new ContinueCallImpl();

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle("io.vertx.starter.MainVerticle");
    log.atInfo().log("Vert.x IVR server is running.");
  }

  @Override
  public void start(Future<Void> fut) {
    Router router = Router.router(vertx);
    router
      .post("/ivr/:appName")
      .handler(this::invokeIvr);

    vertx
      .createHttpServer()
      .requestHandler(router::accept)
      .listen(
        config().getInteger("http.port", 8080),
        result -> {
          if (result.succeeded()) {
            fut.complete();
          } else {
            fut.fail(result.cause());
          }
        }
      );

    vertx.exceptionHandler(new Handler<Throwable>() {
      @Override
      public void handle(Throwable event) {
        log.atWarning().log(event + " throws exception: " + event.getStackTrace());
      }
    });
  }

  private Optional<String> getQueryParam(String name, RoutingContext routingContext) {
    return Optional.ofNullable(routingContext.request().getParam(name));
  }

  public void invokeIvr(RoutingContext routingContext) {
    String appName = routingContext.request().getParam("appName");
    Optional<String> sessionId = getQueryParam("sessionId", routingContext);
    String userInput = getQueryParam("userInput", routingContext).orElse("");
    log.atInfo().log("Received (appName, sessionId, userInput): (%s, %s, %s)",
      appName, sessionId, userInput);

    sessionId.map(id -> this.handleExistingCall(appName, id, userInput))
      .orElseGet(() -> this.handleNewCall(appName))
      .thenAccept(response -> {
        log.atInfo().log("Sending response: %s", response);
        routingContext
          .response()
          .setStatusCode(200)
          .putHeader(HttpHeaders.LOCATION, response.getSessionId())
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/xml")
          .end(response.getDocument());
      })
      .whenComplete((success, error) -> {
        if( error != null) {
          Throwable cause = error.getCause();
          log.atWarning().log("Received: %s", cause);
          if(cause instanceof SessionNotFoundException || cause instanceof ApplicationNotFoundException) {
            routingContext.response().setStatusCode(404).end();
            return;
          }
          routingContext.response().setStatusCode(500).end();
        }
    });
  }

  private CompletableFuture<Response> handleNewCall(String appName) {
    Request useCaseReq = Request.builder()
      .app(appName)
      .build();

    return newCall.handle(useCaseReq);
  }

  private CompletableFuture<Response> handleExistingCall(String appName,
                                                             String id,
                                                             String userInput) {
    Request useCaseReq = Request.builder()
      .app(appName)
      .sessionId(id)
      .userInput(userInput)
      .build();

    return existingCall.handle(useCaseReq);
  }

//  private HttpResponse useCaseResponseToServerResponse(Response response) {
//    log.atInfo().log("Converting use case response to server response: %s", response);
//    return HttpResponse
//      .create()
//      .withStatus(StatusCodes.OK)
//      .addHeader(Location.create(response.getSessionId()))
//      .withEntity(ContentTypes.create(APPLICATION_XML,
//        HttpCharsets.UTF_8), response.getDocument());
//  }
}
