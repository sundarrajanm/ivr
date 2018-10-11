package com.experiment.ivr.usecase.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Builder
@ToString
@Getter
public class Request {
    private String app;
    private String sessionId;
    private String userInput;
    private Map<String, String> params;
    private String document;
}
