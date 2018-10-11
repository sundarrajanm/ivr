package com.experiment.ivr.usecase.impl;

import com.experiment.ivr.core.core.IVRServer;
import com.experiment.ivr.usecase.ContinueCall;
import com.experiment.ivr.usecase.Module;
import com.experiment.ivr.usecase.Utils;
import com.experiment.ivr.usecase.model.Request;
import com.experiment.ivr.usecase.model.Response;
import lombok.extern.flogger.Flogger;

import java.util.concurrent.CompletableFuture;

@Flogger
public class ContinueCallImpl implements ContinueCall {

    private IVRServer server;

    public ContinueCallImpl() {
        this.server = new IVRServer(Module.appStorage(), Module.sessionStorage());
    }

    @Override
    public CompletableFuture<Response> handle(Request request) {
        return server.handleExistingCall(this.toCoreRequest(request))
                .thenApplyAsync(response ->
                        Response.builder()
                                .sessionId(response.getSessionId())
                                .document(Utils.getVXMLDocument(response))
                                .build()
                );
    }

    private com.experiment.ivr.core.core.model.Request toCoreRequest(Request request) {
        return com.experiment.ivr.core.core.model.Request.builder()
                .app(request.getApp())
                .userInput(request.getUserInput())
                .sessionId(request.getSessionId())
                .build();
    }
}
