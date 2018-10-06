package com.experiment.ivr.dataprovider.storage;

import com.experiment.ivr.vxml.model.App;
import com.experiment.ivr.vxml.storage.AppStorage;

import java.util.concurrent.CompletableFuture;

public class AppStorageImpl implements AppStorage {

    @Override
    public CompletableFuture<App> getApplicationByName() {
        return CompletableFuture.completedFuture(new App());
    }

}
