package com.experiment.ivr.core.core.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class App {
    private String id;
    private String name;

    private String startNodeId;
    private List<Node> nodes;
}
