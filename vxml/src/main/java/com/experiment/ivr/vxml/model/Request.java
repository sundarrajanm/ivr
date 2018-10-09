package com.experiment.ivr.vxml.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Request {
    private String uri;
    private String sessionId;
}
