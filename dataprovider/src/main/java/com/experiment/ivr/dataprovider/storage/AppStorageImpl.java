package com.experiment.ivr.dataprovider.storage;

import com.experiment.ivr.vxml.exception.ApplicationNotFoundException;
import com.experiment.ivr.vxml.model.App;
import com.experiment.ivr.vxml.storage.AppStorage;
import com.experiment.ivr.vxml.utils.FutureUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

public class AppStorageImpl implements AppStorage {

    private App someApp = Dummy.app;

    @Override
    public CompletableFuture<App> getApplicationByName(String name) {

        if(StringUtils.equals(someApp.getName(), name)) {
            return CompletableFuture.completedFuture(someApp);
        }

        return FutureUtil.failedFuture(new ApplicationNotFoundException());
    }
}
