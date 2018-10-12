package com.experiment.ivr.runtime.springreactor;

import com.experiment.ivr.core.core.exception.ApplicationNotFoundException;
import com.experiment.ivr.core.core.exception.SessionNotFoundException;
import com.experiment.ivr.usecase.ContinueCall;
import com.experiment.ivr.usecase.StartCall;
import com.experiment.ivr.usecase.model.Request;
import com.experiment.ivr.usecase.model.Response;
import lombok.extern.flogger.Flogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Flogger
public class HTTPHandler {

    private StartCall newCall;

    private ContinueCall existingCall;

    @Autowired
    public HTTPHandler(StartCall newCall, ContinueCall existingCall) {
        this.newCall = newCall;
        this.existingCall = existingCall;
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        String appName = request.pathVariable("app");
        Optional<String> sessionId = request.queryParam("sessionId");
        String userInput = request.queryParam("userInput").orElse("");

        log.atInfo().log("Handing over to app: %s", appName);

        return sessionId
                .map(id -> this.handleExistingCall(appName, id, userInput))
                .orElseGet(() -> this.handleNewCall(appName));
    }

    private Mono<ServerResponse> handleNewCall(String appName) {
        Request useCaseReq = Request.builder()
                .app(appName)
                .build();

        return process(newCall.handle(useCaseReq));
    }

    private Mono<ServerResponse> handleExistingCall(String appName, String sessionId,
                                                    String userInput) {
        Request useCaseReq = Request.builder()
                .app(appName)
                .sessionId(sessionId)
                .userInput(userInput)
                .build();

        return process(existingCall.handle(useCaseReq));
    }

    private Mono<ServerResponse> process(CompletableFuture<Response> handled) {
        Mono<ServerResponse> result = Mono.fromFuture(
                handled.thenComposeAsync(this::useCaseResponseToServerResponse)
        );

        return result.onErrorResume(e -> {
            Throwable t = e.getCause();
            if(t instanceof SessionNotFoundException || t instanceof ApplicationNotFoundException) {
                log.atWarning().log(" Handling: %s", e.getCause());
                return ServerResponse
                        .notFound()
                        .build();
            } else {
                return result; // fallback to default error handling.
            }
        });
    }

    private CompletableFuture<ServerResponse> useCaseResponseToServerResponse(Response response) {
        log.atInfo().log("Converting use case response to server response: %s", response);
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_XML)
                .header(HttpHeaders.LOCATION, response.getSessionId())
                .body(BodyInserters.fromObject(response.getDocument()))
                .toFuture();
    }
}
