package com.experiment.ivr.usecase;

import com.experiment.ivr.core.core.storage.AppStorage;
import com.experiment.ivr.core.core.storage.SessionStorage;
import com.experiment.ivr.dataprovider.storage.AppStorageImpl;
import com.experiment.ivr.dataprovider.storage.SessionStorageImpl;
import com.experiment.ivr.usecase.impl.ContinueCallImpl;
import com.experiment.ivr.usecase.impl.StartCallImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Module extends AbstractModule {

    private static Injector inject = Guice.createInjector(new Module());

    public static StartCall newCall() {
        return new StartCallImpl();
    }

    public static ContinueCall continueCall() {
        return new ContinueCallImpl();
    }

    public static AppStorage appStorage() {
        return inject.getInstance(AppStorage.class);
    }

    public static SessionStorage sessionStorage() {
        return inject.getInstance(SessionStorage.class);
    }

    @Override
    protected void configure() {
        bind(AppStorage.class).to(AppStorageImpl.class).asEagerSingleton();
        bind(SessionStorage.class).to(SessionStorageImpl.class).asEagerSingleton();
    }
}
