package com.experiment.ivr.dataprovider.storage;

import com.experiment.ivr.core.core.exception.ApplicationNotFoundException;
import com.experiment.ivr.core.core.model.App;
import com.experiment.ivr.core.core.storage.AppStorage;
import com.experiment.ivr.core.core.utils.Dummy;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

@Flogger
public class AppStorageImpl implements AppStorage {

    @Override
    public CompletableFuture<App> getApplicationByName(String name) {
        return CompletableFuture.supplyAsync(() -> this.readFromStorage(name));
    }

    private App readFromStorage(String appName) {
        App app = Dummy.getDummyApplication();

        if(StringUtils.equals(app.getName(), appName)) {
            log.atInfo().log("Received app from storage: %s", appName);
            return Dummy.getDummyApplication();
        }

        throw new ApplicationNotFoundException();
    }
}
