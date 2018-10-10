package com.experiment.ivr.core.storage;

import com.experiment.ivr.core.model.App;

import java.util.concurrent.CompletableFuture;

public interface AppStorage {
    CompletableFuture<App> getApplicationByName(String name);
}
