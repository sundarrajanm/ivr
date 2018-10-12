package com.experiment.ivr.runtime.akkahttp;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.flogger.Flogger;

import java.util.concurrent.CompletionStage;

@Flogger
public class Run extends AllDirectives {
    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load();
        ActorSystem system = ActorSystem.create("IVRActorSystem",
                config.getConfig("configuration"));

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        Router httpRouter = new Router();
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = httpRouter
                .routes()
                .flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);
        log.atInfo().log("Server online at http://localhost:8080/\nCtrl-C to stop...");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            binding
                    .thenCompose(ServerBinding::unbind)
                    .thenAccept(unbound -> log.atInfo().log("Server shutdown."));
        }));
    }
}
