package com.experiment.ivr.core.core.exception;

import lombok.ToString;

import java.util.Optional;

@ToString
public class ExitPathNotFoundException extends RuntimeException {
    private Optional<String> exitLabel = Optional.empty();
    public ExitPathNotFoundException(Optional<String> exitLabel) {
        this.exitLabel = exitLabel;
    }
}
