package com.experiment.ivr.vxml;

import com.experiment.ivr.vxml.model.App;
import com.experiment.ivr.vxml.model.Request;
import com.experiment.ivr.vxml.model.Response;
import com.experiment.ivr.vxml.storage.AppStorage;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HandleSessionTest {

    private VXMLServer server;

    private AppStorage storageStub = new AppStorage() {
        @Override
        public CompletableFuture<App> getApplicationByName() {
            return null;
        }
    };

    @BeforeEach
    private void setupMe() {
        server = new VXMLServer(storageStub);
    }

    @Test
    public void GivenNoSessionId_startNewCall() {
        Request request = Request
                .builder()
                .uri("/app1")
                .build();

        Response response = server.handle(request);

        assertNotNull(response, "Server response is null");
        assertTrue(StringUtils.isNotBlank(response.getSessionId()),
                "SessionId is blank");
    }

    @Test
    public void GivenSessionId_respondWithSameSessionId() {
        String id = UUID.randomUUID().toString();

        Request request = Request
                .builder()
                .uri("/app1")
                .sessionId(id)
                .build();

        Response response = server.handle(request);

        assertEquals(id, response.getSessionId(), "SessionId is not same");
    }
}
