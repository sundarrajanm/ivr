package com.experiment.ivr.usecase;

import com.experiment.ivr.usecase.model.Request;
import com.experiment.ivr.usecase.model.Response;

import java.util.concurrent.CompletableFuture;

public interface ContinueCall {
    CompletableFuture<Response> handle(Request request);
}
