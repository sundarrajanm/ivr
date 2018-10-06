package com.experiment.ivr.configuration.springreactor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class Router {
    @Bean
    public RouterFunction<ServerResponse> route(Handler greetingHandler) {
        return RouterFunctions
                .route(RequestPredicates
                        .GET("/start")
                        .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                        greetingHandler::hello);
    }
}
