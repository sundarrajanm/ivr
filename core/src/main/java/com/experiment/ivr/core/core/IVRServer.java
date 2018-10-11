package com.experiment.ivr.core.core;

import com.experiment.ivr.core.core.exception.ExitPathNotFoundException;
import com.experiment.ivr.core.core.exception.SessionAccessException;
import com.experiment.ivr.core.core.exception.SessionNotFoundException;
import com.experiment.ivr.core.core.model.*;
import com.experiment.ivr.core.core.storage.AppStorage;
import com.experiment.ivr.core.core.storage.SessionStorage;
import com.experiment.ivr.core.core.utils.FutureUtil;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * VXML Server is the core class of VXML Module. This is not yet an interface
 * because we do not foresee multiple or decoupled implementations.
 */

@Flogger
public class IVRServer {

    private AppStorage appStorage;

    private SessionStorage sessionStorage;

    public IVRServer(AppStorage appStorage, SessionStorage sessionStorage) {
        this.appStorage = appStorage;
        this.sessionStorage = sessionStorage;
    }

    public CompletableFuture<Response> handleNewCall(Request request) {
        String appName = this.appNameFromUri(request);

        CompletableFuture<App> appFuture = appStorage.getApplicationByName(appName);
        return appFuture.thenComposeAsync(app ->
            sessionStorage.createNewSessionWithId()
                    .thenApplyAsync(session -> this.newCall(app, session, request))
        );
    }

    public CompletableFuture<Response> handleExistingCall(Request request) {
        String appName = this.appNameFromUri(request);
        Optional<String> sessionId = this.getSessionIdFrom(request);

        if(sessionId.isPresent()) {
            CompletableFuture<App> appFuture = appStorage.getApplicationByName(appName);
            CompletableFuture<Session> sessionFuture = sessionStorage.fetchSessionById(sessionId.get());
            return CompletableFuture
                    .allOf(appFuture, sessionFuture)
                    .thenApplyAsync(v -> resumeFromCurrentNode(request, appFuture, sessionFuture));
        }

        return FutureUtil.failedFuture(new SessionNotFoundException());
    }

    private Response resumeFromCurrentNode(Request request, CompletableFuture<App> appFuture, CompletableFuture<Session> sessionFuture) {
        Session session = sessionFuture.join();
        App app = appFuture.join();
        return Optional.ofNullable(session.getData(Session.KEYS.CURRENT_NODE_ID.getValue()))
                .map(Object::toString)
                .map(currentNodeId -> this.continueCall(app, session, request, currentNodeId))
                .orElseThrow(() -> new SessionAccessException());
    }

    private Response newCall(App app, Session sess, Request request) {
        log.atInfo().log("Starting new session: %s", sess);
        sess.putData(Session.KEYS.CURRENT_NODE_ID.getValue(), app.getStartNodeId());
        sessionStorage.updateSession(sess);

        return app.getNodes()
                .stream()
                .filter(n -> n.getId() == app.getStartNodeId())
                .findFirst()
                .map(n -> Response.builder()
                        .sessionId(sess.getCallId())
                        .prompt(n.getPrompt())
                        .type(n.getType())
                        .build())
                .orElse(Response.builder()
                        .sessionId(sess.getCallId())
                        .build());
    }

    private Response continueCall(App app, Session sess, Request request, String currentNodeId) {
        log.atInfo().log("Trying to find next node from %s in the session %s",
                currentNodeId, sess.getCallId());
        Optional<Node> nextNode = app
                .getNodes()
                .stream()
                .filter(n -> n.getId() == currentNodeId)
                .findFirst()
                .map(n -> this.findNextNode(n, Optional.ofNullable(request.getUserInput())));

        if(nextNode.isPresent()) {
            Node n = nextNode.get();
            log.atInfo().log("Executing next node: %s", n.getName());
            Response response = Response.builder()
                    .prompt(n.getPrompt())
                    .type(n.getType())
                    .sessionId(sess.getCallId())
                    .lastResponse(n.getExits().size() == 0)
                    .build();

            if(response.isLastResponse()) {
                sessionStorage.removeSession(sess);
            } else {
                sess.putData(Session.KEYS.CURRENT_NODE_ID.getValue(), n.getId());
                sessionStorage.updateSession(sess);
            }
            return response;
        }

        return Response.builder()
                .sessionId(sess.getCallId())
                .build();
    }

    private Node findNextNode(Node currentNode, Optional<String> exitLabel) {
        if(currentNode.getExits().size() == 0) {
            throw new ExitPathNotFoundException(exitLabel);
        }

        if(currentNode.getType() == Node.Type.CHOICE) {
            return currentNode
                    .getExits()
                    .stream()
                    .filter(e -> e.getValue().isPresent() && e.getValue().equals(exitLabel))
                    .findFirst()
                    .map(e -> e.getConnectTo())
                    .orElseThrow(() -> new ExitPathNotFoundException(exitLabel));
        }

        return currentNode.getExits().get(0).getConnectTo();
    }

    private String appNameFromUri(Request request) {
        String uri = request.getApp();
        String app = "";

        if(!uri.startsWith("/") && StringUtils.isNotBlank(uri)) {
            app = uri;
        }

        if(uri.startsWith("/")) {
            app = uri.substring(1);
        }

        log.atInfo().log("Trying to use application: %s", app);
        return app;
    }

    private Optional<String> getSessionIdFrom(Request request) {
        return Optional.ofNullable(request.getSessionId());
    }
}
