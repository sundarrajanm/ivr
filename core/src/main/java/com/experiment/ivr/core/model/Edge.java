package com.experiment.ivr.core.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Builder
@Getter
public class Edge {
    private String id;
    private Optional<String> value;
    private Node connectTo;
}
