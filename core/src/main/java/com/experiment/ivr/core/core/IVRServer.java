package com.experiment.ivr.core.core;

import com.experiment.ivr.core.core.exception.ExitPathNotFoundException;
import com.experiment.ivr.core.core.model.*;
import com.experiment.ivr.core.core.storage.AppStorage;
import com.experiment.ivr.core.core.storage.SessionStorage;
import lombok.extern.flogger.Flogger;

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

    public CompletableFuture<Response> handle(Request request) {
        log.atInfo().log("Handling %s", request);
        String appName = this.appNameFromUri(request);

        return appStorage
                .getApplicationByName(appName)
                .thenComposeAsync(app -> this.handleRequestWith(app, request));
    }

    private CompletableFuture<Response> handleRequestWith(App app, Request request) {

        Optional<String> sessionId = this.getSessionIdFrom(request);
        log.atInfo().log("Session in request: %s", sessionId);
        CompletableFuture<Session> session = sessionId
                .map(sessionStorage::fetchSessionById)
                .orElseGet(sessionStorage::createNewSessionWithId);

        return session
                .thenApplyAsync(sess -> this.executeNodeWith(app, sess, request));
    }

    private Response executeNodeWith(App app, Session sess, Request req) {
        return Optional.ofNullable(sess.getData(Session.KEYS.CURRENT_NODE_ID.toString()))
                .map(Object::toString)
                .map(currentNodeId -> this.continueCall(app, sess, req, currentNodeId))
                .orElseGet(() -> this.newCall(app, sess, req));
    }

    private Response newCall(App app, Session sess, Request request) {
        log.atInfo().log("Starting new session for %s", sess.getCallId());
        sess.putData(Session.KEYS.CURRENT_NODE_ID.toString(), app.getStartNodeId());
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
        log.atInfo().log("Trying to find next node from %s in the session %s", currentNodeId, sess.getCallId());
        Optional<Node> nextNode = app
                .getNodes()
                .stream()
                .filter(n -> n.getId() == currentNodeId)
                .findFirst()
                .map(n -> this.findNextNode(n, Optional.ofNullable(request.getUserInput())));

        if(nextNode.isPresent()) {
            Node n = nextNode.get();
            log.atInfo().log("Executing next node: %s", n.getName());
            sess.putData(Session.KEYS.CURRENT_NODE_ID.toString(), n.getId());
            sessionStorage.updateSession(sess);
            return Response.builder()
                    .prompt(n.getPrompt())
                    .type(n.getType())
                    .sessionId(sess.getCallId())
                    .lastResponse(n.getExits().size() == 0)
                    .build();
        }

        return Response.builder()
                .sessionId(sess.getCallId())
                .build();
    }

    private Node findNextNode(Node currentNode, Optional<String> exitLabel) {
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
        String uri = request.getUri();
        String app = "";

        if(uri.startsWith("/")) {
            app = uri.substring(1);
        }

        log.atInfo().log("Using application: %s", app);
        return app;
    }

    private Optional<String> getSessionIdFrom(Request request) {
        return Optional.ofNullable(request.getSessionId());
    }
}
