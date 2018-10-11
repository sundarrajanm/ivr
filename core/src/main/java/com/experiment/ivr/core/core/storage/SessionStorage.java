package com.experiment.ivr.core.core.storage;

import com.experiment.ivr.core.core.model.Session;

import java.util.concurrent.CompletableFuture;

public interface SessionStorage {
    CompletableFuture<Session> fetchSessionById(String id);
    CompletableFuture<Session> createNewSessionWithId();
    CompletableFuture<Boolean> updateSession(Session session);
    CompletableFuture<Boolean> removeSession(Session session);
}
