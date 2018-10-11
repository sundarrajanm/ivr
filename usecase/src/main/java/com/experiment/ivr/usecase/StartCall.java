package com.experiment.ivr.usecase;

import com.experiment.ivr.usecase.model.Request;
import com.experiment.ivr.usecase.model.Response;

import java.util.concurrent.CompletableFuture;

public interface StartCall {
    CompletableFuture<Response> handle(Request request);
}
