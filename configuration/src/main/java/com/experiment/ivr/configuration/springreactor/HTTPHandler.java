package com.experiment.ivr.configuration.springreactor;

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

        return Mono.fromFuture(newCall
                .handle(useCaseReq)
                .thenComposeAsync(this::useCaseResponseToServerResponse)
        );
    }

    private Mono<ServerResponse> handleExistingCall(String appName, String sessionId,
                                                    String userInput) {
        Request useCaseReq = Request.builder()
                .app(appName)
                .sessionId(sessionId)
                .userInput(userInput)
                .build();

        return Mono
                .fromFuture(existingCall
                        .handle(useCaseReq)
                        .thenComposeAsync(this::useCaseResponseToServerResponse)
                );
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
