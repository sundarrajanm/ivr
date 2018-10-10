package com.experiment.ivr.core.core.storage;

import com.experiment.ivr.core.core.model.App;

import java.util.concurrent.CompletableFuture;

public interface AppStorage {
    CompletableFuture<App> getApplicationByName(String name);
}
