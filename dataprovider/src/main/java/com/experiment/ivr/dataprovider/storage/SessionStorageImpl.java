package com.experiment.ivr.dataprovider.storage;

import com.experiment.ivr.vxml.model.Session;
import com.experiment.ivr.vxml.storage.SessionStorage;

import java.util.concurrent.CompletableFuture;

public class SessionStorageImpl implements SessionStorage {

    @Override
    public CompletableFuture<Session> fetchSessionByCallId(String id) {
        return CompletableFuture.completedFuture(new Session());
    }

}
