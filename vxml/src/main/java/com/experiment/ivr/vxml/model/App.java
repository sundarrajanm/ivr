package com.experiment.ivr.vxml.model;

import lombok.Builder;

import java.util.List;

@Builder
public class App {
    private String id;
    private String name;

    private String startNodeId;
    private List<Node> nodes;
}
