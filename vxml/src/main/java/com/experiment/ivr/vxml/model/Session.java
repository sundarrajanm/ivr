package com.experiment.ivr.vxml.model;

import lombok.Data;

@Data
public class Session {
    private String calledNumber;
    private String callingNumber;
    private String callId;
}
