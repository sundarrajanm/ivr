package com.experiment.ivr.dataprovider.storage;

import com.experiment.ivr.core.exception.ApplicationNotFoundException;
import com.experiment.ivr.core.model.App;
import com.experiment.ivr.core.storage.AppStorage;
import com.experiment.ivr.core.utils.FutureUtil;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

@Flogger
public class AppStorageImpl implements AppStorage {

    private App someApp = Dummy.app;

    @Override
    public CompletableFuture<App> getApplicationByName(String name) {

        if(StringUtils.equals(someApp.getName(), name)) {
            return CompletableFuture.supplyAsync(() -> this.readFromStorage(name));
        }

        return FutureUtil.failedFuture(new ApplicationNotFoundException());
    }

    private App readFromStorage(String appName) {
        log.atInfo().log("Received app from storage: %s", appName);
        return someApp;
    }
}
