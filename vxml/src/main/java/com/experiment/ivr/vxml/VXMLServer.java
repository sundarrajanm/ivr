package com.experiment.ivr.vxml;

import com.experiment.ivr.vxml.model.App;
import com.experiment.ivr.vxml.model.Request;
import com.experiment.ivr.vxml.model.Response;
import com.experiment.ivr.vxml.model.Session;
import com.experiment.ivr.vxml.storage.AppStorage;
import com.experiment.ivr.vxml.storage.SessionStorage;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * VXML Server is the core class of VXML Module. This is not yet an interface
 * because we do not foresee multiple or decoupled implementations.
 */

@Flogger
public class VXMLServer {

    private AppStorage appStorage;

    private SessionStorage sessionStorage;

    public VXMLServer(AppStorage appStorage, SessionStorage sessionStorage) {
        this.appStorage = appStorage;
        this.sessionStorage = sessionStorage;
    }

    public CompletableFuture<Response> handle(Request request) {
        this.logCurrentThread("handle");
        String appName = this.appNameFromUri(request);

        return appStorage
                .getApplicationByName(appName)
                .thenComposeAsync(app -> this.handleRequestWith(app, request));
    }

    private CompletableFuture<Response> handleRequestWith(App app, Request request) {

        this.logCurrentThread("handleRequestWith");
        Optional<String> sessionId = this.getSessionIdFrom(request);
        CompletableFuture<Session> session = sessionId
                .map(sessionStorage::fetchSessionById)
                .orElseGet(sessionStorage::createNewSessionWithId);

        return session
                .thenApplyAsync(sess -> this.executeNodeWith(app, sess, request));
    }

    private void logCurrentThread(String from) {
        log.at(Level.INFO)
                .log(from + ": Thread: " + Thread.currentThread().getName());
    }

    private Response executeNodeWith(App app, Session sess, Request request) {
        this.logCurrentThread("executeNodeWith");
        return Response.builder()
                .sessionId(sess.getCallId())
                .build();
    }

    private String appNameFromUri(Request request) {
        String uri = request.getUri();

        if(StringUtils.isBlank(uri)) {
            return "";
        }

        if(uri.startsWith("/")) {
            return uri.substring(1);
        }

        return uri;
    }

    private Optional<String> getSessionIdFrom(Request request) {
        return Optional.ofNullable(request.getSessionId());
    }
}
