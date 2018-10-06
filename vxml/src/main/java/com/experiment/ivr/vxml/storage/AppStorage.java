package com.experiment.ivr.vxml.storage;

import com.experiment.ivr.vxml.model.App;

import java.util.concurrent.CompletableFuture;

public interface AppStorage {
    CompletableFuture<App> getApplicationByName();
}
