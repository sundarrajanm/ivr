package com.experiment.ivr.core.core.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Request {
    private String app;
    private String sessionId;
    private String userInput;
}
