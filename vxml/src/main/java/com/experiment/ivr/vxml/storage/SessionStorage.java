package com.experiment.ivr.vxml.storage;

import com.experiment.ivr.vxml.model.Session;

import java.util.concurrent.CompletableFuture;

public interface SessionStorage {
    CompletableFuture<Session> fetchSessionByCallId(String id);
}