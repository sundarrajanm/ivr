package com.experiment.ivr.dataprovider.storage;

import com.experiment.ivr.vxml.exception.SessionNotFoundException;
import com.experiment.ivr.vxml.model.Session;
import com.experiment.ivr.vxml.storage.SessionStorage;
import com.experiment.ivr.vxml.utils.FutureUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStorageImpl implements SessionStorage {

    private Map<String, Session> cache = new ConcurrentHashMap();

    @Override
    public CompletableFuture<Session> fetchSessionById(String id) {

        Session session = cache.get(id);

        if( session == null ) {
            return FutureUtil.failedFuture(new SessionNotFoundException());
        }

        return CompletableFuture.completedFuture(session);
    }

    @Override
    public CompletableFuture<Session> createNewSessionWithId() {
        Session session = new Session();
        session.setCallId(this.createNewSessionId());
        cache.put(session.getCallId(), session);
        return CompletableFuture.completedFuture(session);
    }

    private String createNewSessionId() {
        return UUID.randomUUID().toString();
    }

}
