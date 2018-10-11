package com.experiment.ivr.core.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class App {
    private String id;
    private String name;

    private String startNodeId;
    private List<Node> nodes;
}
