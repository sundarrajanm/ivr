package com.experiment.ivr.usecase.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Response {
    private String document;
    private String sessionId;
}
