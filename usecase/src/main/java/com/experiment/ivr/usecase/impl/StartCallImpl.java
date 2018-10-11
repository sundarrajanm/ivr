package com.experiment.ivr.usecase.impl;

import com.experiment.ivr.core.core.IVRServer;
import com.experiment.ivr.usecase.Module;
import com.experiment.ivr.usecase.StartCall;
import com.experiment.ivr.usecase.Utils;
import com.experiment.ivr.usecase.model.Request;
import com.experiment.ivr.usecase.model.Response;
import lombok.extern.flogger.Flogger;

import java.util.concurrent.CompletableFuture;

@Flogger
public class StartCallImpl implements StartCall {

    private IVRServer server;

    public StartCallImpl() {
        this.server = new IVRServer(Module.appStorage(), Module.sessionStorage());
    }

    @Override
    public CompletableFuture<Response> handle(Request request) {
        log.atInfo().log("Starting new call to IVR Server: " + server);
        server.handle(this.toCoreRequest(request));
        return CompletableFuture.completedFuture(
                Response.builder()
                        .document(Utils.readTestXML("NewCallResponse.xml"))
                        .build()
        );
    }


    private com.experiment.ivr.core.core.model.Request toCoreRequest(Request request) {
        return com.experiment.ivr.core.core.model.Request.builder()
                .uri(request.getApp())
                .sessionId(request.getSessionId())
                .build();
    }
}
