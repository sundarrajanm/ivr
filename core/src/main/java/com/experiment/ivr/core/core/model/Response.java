package com.experiment.ivr.core.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Response {
    private String sessionId;
    private String prompt;
    private Node.Type type;
    private boolean lastResponse;
}
