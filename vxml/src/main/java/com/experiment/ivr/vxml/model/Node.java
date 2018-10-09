package com.experiment.ivr.vxml.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Builder
@Getter
public class Node {
    public enum Type {
        PROMPT,
        CHOICE
    }

    private String id;
    private String name;
    private String prompt;
    private Type type;
    private List<Edge> exits;

    public void connectTo(Node node, Optional<String> exitValue) {
        Edge e = Edge.builder()
                .id(UUID.randomUUID().toString())
                .value(exitValue)
                .connectTo(node)
                .build();
        this.exits.add(e);
    }
}
