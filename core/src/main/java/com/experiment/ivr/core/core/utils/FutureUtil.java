package com.experiment.ivr.core.core.utils;

import java.util.concurrent.CompletableFuture;

public class FutureUtil {
    public static <R> CompletableFuture<R> failedFuture(Throwable error) {
        CompletableFuture<R> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }
}
