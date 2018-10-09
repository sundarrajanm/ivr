package com.experiment.ivr.vxml;

import com.experiment.ivr.vxml.model.Request;
import com.experiment.ivr.vxml.model.Response;
import com.experiment.ivr.vxml.storage.AppStorage;

import java.util.Optional;
import java.util.UUID;

/**
 * VXML Server is the core class of VXML Module. This is not yet an interface
 * because we do not foresee multiple or decoupled implementations.
 */

public class VXMLServer {
    private AppStorage appStorage;

    public VXMLServer(AppStorage appStorage) {
        this.appStorage = appStorage;
    }

    public Response handle(Request request) {

        String id = this.getSessionIdFrom(request)
                .orElse(this.createNewSessionId());

        return Response.builder()
                .sessionId(id)
                .build();
    }

    private Optional<String> getSessionIdFrom(Request request) {
        return Optional.ofNullable(request.getSessionId());
    }

    private String createNewSessionId() {
        return UUID.randomUUID().toString();
    }
}
