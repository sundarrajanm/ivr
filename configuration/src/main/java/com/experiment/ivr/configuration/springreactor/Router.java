package com.experiment.ivr.configuration.springreactor;

import com.experiment.ivr.usecase.ContinueCall;
import com.experiment.ivr.usecase.StartCall;
import com.experiment.ivr.usecase.impl.ContinueCallImpl;
import com.experiment.ivr.usecase.impl.StartCallImpl;
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
    public StartCall startCall() {
        return new StartCallImpl();
    }

    @Bean
    public ContinueCall continueCall() {
        return new ContinueCallImpl();
    }

    @Bean
    public RouterFunction<ServerResponse> route(HTTPHandler ivr) {
        return RouterFunctions
                .route(RequestPredicates
                        .POST("/ivr/{app}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_XML)),
                        ivr::handle);
    }
}
