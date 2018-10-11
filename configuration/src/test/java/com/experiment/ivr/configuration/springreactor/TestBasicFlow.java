package com.experiment.ivr.configuration.springreactor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
        String newCallResponseVxml = this.readTestXML("NewCallResponse.xml");

        webTestClient
                .post().uri("/ivr/dummy")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(newCallResponseVxml);
    }

    @Test
    public void PostToIVREndpointWithSessionId_TriggersContinueExistingCall() {
        String existingCallResponseVxml = this.readTestXML("ExistingCallResponse.xml");
        String sessionId = UUID.randomUUID().toString();

        webTestClient
                .post().uri("/ivr/dummy?sessionId=" + sessionId)
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(existingCallResponseVxml);
    }
}
