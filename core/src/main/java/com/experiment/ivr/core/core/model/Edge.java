package com.experiment.ivr.core.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Builder
@Getter
@ToString
public class Edge {
    private String id;
    private Optional<String> value;
    private Node connectTo;
}
