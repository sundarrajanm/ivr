package com.experiment.ivr.dataprovider;

import com.experiment.ivr.dataprovider.storage.AppStorageImpl;
import com.experiment.ivr.vxml.VXMLServer;
import com.experiment.ivr.vxml.storage.AppStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DummyAppTest {

    @BeforeEach
    private void setupMe() {
        AppStorage appStorage = new AppStorageImpl();
        VXMLServer server = new VXMLServer(appStorage);

    }

    @Test
    public void startAppTest () {
        System.out.println("Works");
    }
}
