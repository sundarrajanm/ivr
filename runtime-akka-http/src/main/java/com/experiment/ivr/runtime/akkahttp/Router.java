package com.experiment.ivr.runtime.akkahttp;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpCharsets;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import com.experiment.ivr.core.core.exception.ApplicationNotFoundException;
import com.experiment.ivr.core.core.exception.SessionNotFoundException;
import com.experiment.ivr.usecase.ContinueCall;
import com.experiment.ivr.usecase.StartCall;
import com.experiment.ivr.usecase.impl.ContinueCallImpl;
import com.experiment.ivr.usecase.impl.StartCallImpl;
import com.experiment.ivr.usecase.model.Request;
import com.experiment.ivr.usecase.model.Response;
import lombok.extern.flogger.Flogger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static akka.http.javadsl.model.MediaTypes.APPLICATION_XML;

@Flogger
public class Router extends AllDirectives {

    private StartCall newCall = new StartCallImpl();

    private ContinueCall existingCall = new ContinueCallImpl();

    public Route routes() {
        final ExceptionHandler exceptionHandler = ExceptionHandler
                .newBuilder()
                .match(CompletionException.class,
                        e -> e.getCause() instanceof SessionNotFoundException,
                        x -> complete(StatusCodes.NOT_FOUND, "Unable to find session"))
                .match(CompletionException.class,
                        e -> e.getCause() instanceof ApplicationNotFoundException,
                        x -> complete(StatusCodes.NOT_FOUND, "Unable to find application"))
                .build();

        Route allRoutes = post(() -> route(
                path(PathMatchers.segment("ivr").slash(PathMatchers.segment()), appName ->
                        route(parameterList(params ->
                                completeWithFuture(this.handle(appName, params))
                        )
                )
        )));

        return handleExceptions(exceptionHandler, () -> allRoutes);
    }

    private CompletableFuture<HttpResponse> handle(String appName,
                                                   List<Map.Entry<String, String>> params) {
        List<Map.Entry> items = params
                .stream()
                .filter(e -> Arrays.asList("sessionId", "userInput")
                .contains(e.getKey()))
                .collect(Collectors.toList());

        Optional<String> sessionId = items
                .stream()
                .filter(e -> e.getKey().equals("sessionId"))
                .findFirst()
                .map(e -> e.getValue().toString());

        String userInput = items
                .stream()
                .filter(e -> e.getKey().equals("userInput"))
                .findFirst()
                .map(e -> e.getValue().toString()).orElse("");


        return sessionId.map(id -> this.handleExistingCall(appName, id, userInput))
                .orElseGet(() -> this.handleNewCall(appName));
    }

    private CompletableFuture<HttpResponse> handleNewCall(String appName) {
        Request useCaseReq = Request.builder()
                .app(appName)
                .build();

        return newCall.handle(useCaseReq)
                .thenApply(this::useCaseResponseToServerResponse);
    }

    private CompletableFuture<HttpResponse> handleExistingCall(String appName,
                                                               String id,
                                                               String userInput) {
        Request useCaseReq = Request.builder()
                .app(appName)
                .sessionId(id)
                .userInput(userInput)
                .build();

        return existingCall.handle(useCaseReq)
                .thenApply(this::useCaseResponseToServerResponse);
    }

    private HttpResponse useCaseResponseToServerResponse(Response response) {
        log.atInfo().log("Converting use case response to server response: %s", response);
        return HttpResponse
                .create()
                .withStatus(StatusCodes.OK)
                .addHeader(Location.create(response.getSessionId()))
                .withEntity(ContentTypes.create(APPLICATION_XML,
                        HttpCharsets.UTF_8), response.getDocument());
    }
}
