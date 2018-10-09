package com.experiment.ivr.dataprovider.storage;

import com.experiment.ivr.vxml.model.App;
import com.experiment.ivr.vxml.model.Node;
import com.experiment.ivr.vxml.storage.AppStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AppStorageImpl implements AppStorage {

    private App dummyApp;

    public AppStorageImpl() {
        dummyApp = this.dummyApplication();
    }

    @Override
    public CompletableFuture<App> getApplicationByName() {
        return CompletableFuture.completedFuture(dummyApp);
    }

    private Node constructPromptNodeByName(String name,
                                           Optional<String> prompt) {
        return Node.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .exits(new ArrayList<>())
                .type(Node.Type.PROMPT)
                .prompt(prompt.orElse(""))
                .build();
    }

    public App dummyApplication() {

        Node start = this.constructPromptNodeByName("Start", Optional.of("Hello, Welcome to Cisco Cloud VXML Server"));
        Node choice = this.constructPromptNodeByName("DrinkType", Optional.of("Do you want a Beer or Tea?"));
        Node beer = this.constructPromptNodeByName("Beer", Optional.of("Excellent choice."));
        Node tea = this.constructPromptNodeByName("Tea", Optional.of("Not a bad choice."));
        Node end = this.constructPromptNodeByName("End", Optional.empty());

        start.connectTo(choice, Optional.empty());
        choice.connectTo(beer, Optional.of("beer"));
        choice.connectTo(tea, Optional.of("tea"));
        beer.connectTo(end, Optional.empty());
        tea.connectTo(end, Optional.empty());

        return App.builder()
                .id(UUID.randomUUID().toString())
                .name("Dummy Application")
                .nodes(Arrays.asList(start, choice, beer, tea, end))
                .startNodeId(start.getId())
                .build();
    }
}
