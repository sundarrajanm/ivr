package com.experiment.ivr.runtime.springreactor;

import com.experiment.ivr.core.core.model.Node;
import com.experiment.ivr.core.core.model.Response;
import com.experiment.ivr.usecase.Utils;
import com.google.common.net.HttpHeaders;
import lombok.extern.flogger.Flogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Flogger
public class TestBasicFlow {

    @Autowired
    private WebTestClient webTestClient;

    private String readTestXML(String name) {
        try {
            return new String(Files.readAllBytes(Paths.get(
                    ClassLoader.getSystemResource(name).toURI()
            )));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail("Unable to read the text XML file: " + name);
        }
        return "";
    }

    @Test
    public void PostToIVREndpoint_TriggersNewCall() {
        String newCallResponseVxml = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.PROMPT)
                        .prompt("Hello, Welcome to Cisco Cloud IVR Server")
                        .build()
        );

        webTestClient
                .post().uri("/ivr/dummy")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(String.class).isEqualTo(newCallResponseVxml);
    }

    @Test
    public void PostToIVREndpointWithSessionId_TriggersContinueExistingCall() {
        String newCallResponseVxml = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.PROMPT)
                        .prompt("Hello, Welcome to IVR Server")
                        .build()
        );
        String existingCallResponseVxml = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.CHOICE)
                        .prompt("Do you want a Beer or Tea?")
                        .build()
        );

        EntityExchangeResult result = webTestClient
                .post().uri("/ivr/dummy")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(String.class).isEqualTo(newCallResponseVxml)
                .returnResult();

        String sessionId = result.getResponseHeaders().getLocation().toString();

        webTestClient
                .post().uri("/ivr/dummy?sessionId=" + sessionId)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, sessionId)
                .expectBody(String.class).isEqualTo(existingCallResponseVxml);
    }

    @Test
    public void PostToIVREndpointWithSessionId_AndUserInput_TriggersContinueOnRightExitPath() {
        String newCallResponseVxml = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.PROMPT)
                        .prompt("Hello, Welcome to IVR Server")
                        .build()
        );
        String existingCallResponseVxml = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.CHOICE)
                        .prompt("Do you want a Beer or Tea?")
                        .build()
        );
        String lastButOneResponse = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.PROMPT)
                        .prompt("Not a bad choice.")
                        .build()
        );

        EntityExchangeResult result = webTestClient
                .post().uri("/ivr/dummy")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(String.class).isEqualTo(newCallResponseVxml)
                .returnResult();

        String sessionId = result.getResponseHeaders().getLocation().toString();

        webTestClient
                .post().uri("/ivr/dummy?sessionId=" + sessionId)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, sessionId)
                .expectBody(String.class).isEqualTo(existingCallResponseVxml);

        webTestClient
                .post().uri("/ivr/dummy?sessionId=" + sessionId + "&userInput=tea")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, sessionId)
                .expectBody(String.class).isEqualTo(lastButOneResponse);
    }

    @Test
    public void PostToIVREndpointWithSessionId_ToLastRequest_TriggersContinueAndClearsSession() {
        String newCallResponseVxml = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.PROMPT)
                        .prompt("Hello, Welcome to IVR Server")
                        .build()
        );
        String existingCallResponseVxml = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.CHOICE)
                        .prompt("Do you want a Beer or Tea?")
                        .build()
        );
        String lastButOneResponse = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.PROMPT)
                        .prompt("Not a bad choice.")
                        .build()
        );
        String lastResponse = Utils.getVXMLDocument(
                Response.builder()
                        .type(Node.Type.PROMPT)
                        .prompt("")
                        .build()
        );

        EntityExchangeResult result = webTestClient
                .post().uri("/ivr/dummy")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(String.class).isEqualTo(newCallResponseVxml)
                .returnResult();

        String sessionId = result.getResponseHeaders().getLocation().toString();

        webTestClient
                .post().uri("/ivr/dummy?sessionId=" + sessionId)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, sessionId)
                .expectBody(String.class).isEqualTo(existingCallResponseVxml);

        webTestClient
                .post().uri("/ivr/dummy?sessionId=" + sessionId + "&userInput=tea")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, sessionId)
                .expectBody(String.class).isEqualTo(lastButOneResponse);

        webTestClient
                .post().uri("/ivr/dummy?sessionId=" + sessionId)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, sessionId)
                .expectBody(String.class).isEqualTo(lastResponse);

        // Call Ended, subsequent request to the same session should return 404
        webTestClient
                .post().uri("/ivr/dummy?sessionId=" + sessionId)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isNotFound();
    }
}
