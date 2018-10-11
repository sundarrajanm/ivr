package com.experiment.ivr.core.core.utils;

import com.experiment.ivr.core.core.model.App;
import com.experiment.ivr.core.core.model.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class Dummy {
    public static final String APP_NAME = "dummy";
    public static App app = dummyApplication();

    private static Node constructPromptNodeByName(String name,
                                           Optional<String> prompt) {
        return Node.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .exits(new ArrayList<>())
                .type(Node.Type.PROMPT)
                .prompt(prompt.orElse(""))
                .build();
    }

    private static Node constructChoiceNodeByName(String name,
                                                  Optional<String> prompt) {
        return Node.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .exits(new ArrayList<>())
                .type(Node.Type.CHOICE)
                .prompt(prompt.orElse(""))
                .build();
    }

    public static App getDummyApplication() {
        return app;
    }

    private static App dummyApplication() {

        Node start = constructPromptNodeByName("Start", Optional.of("Hello, Welcome to Cisco Cloud IVR Server"));
        Node choice = constructChoiceNodeByName("DrinkType", Optional.of("Do you want a Beer or Tea?"));
        Node beer = constructPromptNodeByName("Beer", Optional.of("Excellent choice."));
        Node tea = constructPromptNodeByName("Tea", Optional.of("Not a bad choice."));
        Node end = constructPromptNodeByName("End", Optional.empty());

        start.connectTo(choice, Optional.empty());
        choice.connectTo(beer, Optional.of("beer"));
        choice.connectTo(tea, Optional.of("tea"));
        beer.connectTo(end, Optional.empty());
        tea.connectTo(end, Optional.empty());

        return App.builder()
                .id(UUID.randomUUID().toString())
                .name(APP_NAME)
                .nodes(Arrays.asList(start, choice, beer, tea, end))
                .startNodeId(start.getId())
                .build();
    }
}
