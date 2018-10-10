package com.experiment.ivr.dataprovider.storage;

import com.experiment.ivr.core.core.model.App;
import com.experiment.ivr.core.core.storage.AppStorage;
import lombok.extern.flogger.Flogger;

import java.util.concurrent.CompletableFuture;

@Flogger
public class AppStorageImpl implements AppStorage {

    @Override
    public CompletableFuture<App> getApplicationByName(String name) {
        return CompletableFuture.supplyAsync(() -> this.readFromStorage(name));
    }

    private App readFromStorage(String appName) {
        log.atInfo().log("Received app from storage: %s", appName);
        return App.builder().build();
    }
}
