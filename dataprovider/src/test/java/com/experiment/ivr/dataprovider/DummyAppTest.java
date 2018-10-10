package com.experiment.ivr.dataprovider;

import com.experiment.ivr.core.IVRServer;
import com.experiment.ivr.core.exception.ApplicationNotFoundException;
import com.experiment.ivr.core.model.Node;
import com.experiment.ivr.core.model.Request;
import com.experiment.ivr.core.model.Response;
import com.experiment.ivr.core.model.Session;
import com.experiment.ivr.core.storage.AppStorage;
import com.experiment.ivr.core.storage.SessionStorage;
import com.experiment.ivr.core.utils.FutureUtil;
import com.experiment.ivr.dataprovider.storage.Dummy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DummyAppTest {

    @Mock
    private AppStorage appStorage;

    @Mock
    private SessionStorage sessionStorage;

    @InjectMocks
    private IVRServer server;

    @Test
    public void startUnknownApplication_ShouldReturnFailure () {

        willReturn(FutureUtil.failedFuture(
                new ApplicationNotFoundException()))
                .given(appStorage).getApplicationByName("test");

        Request request = Request.builder().uri("/test").build();
        Executable e = () -> server.handle(request).join();

        RuntimeException exception = assertThrows(
                CompletionException.class, e);
        assertThat(exception.getCause()).isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    public void startKnownApplication_ShouldUpdateSession_AndReturnFirstResponse() {

        Session session = new Session();
        session.setCallId(UUID.randomUUID().toString());
        mockAppCallsToDummyApp();
        willReturn(CompletableFuture.completedFuture(session))
                .given(sessionStorage)
                .createNewSessionWithId();

        Request request = Request.builder()
                .uri("/" + Dummy.APP_NAME)
                .build();

        Response response = server.handle(request).join();
        assertThat(session.getData(Session.KEYS.CURRENT_NODE_ID.toString())).isEqualTo(Dummy.app.getStartNodeId());
        verify(sessionStorage, times(1)).updateSession(session);
        assertThat(response).isNotNull();
        assertThat(response.getPrompt()).isEqualTo("Hello, Welcome to Cisco Cloud IVR Server");
        assertThat(response.getType()).isEqualTo(Node.Type.PROMPT);
        assertThat(response.isLastResponse()).isFalse();
    }

    @Test
    public void continueKnownApplicationFromCurrentNode_ShouldUpdateNextNodeAsCurrentNode_AndReturnSubsequentResponse () {

        Session session = new Session();
        session.putData(Session.KEYS.CURRENT_NODE_ID.toString(), Dummy.app.getStartNodeId());
        session.setCallId(UUID.randomUUID().toString());

        Request request = Request.builder()
                .uri("/" + Dummy.APP_NAME)
                .sessionId(session.getCallId())
                .build();

        mockAppCallsToDummyApp();
        willReturn(CompletableFuture.completedFuture(session))
                .given(sessionStorage)
                .fetchSessionById(session.getCallId());

        Response response = server.handle(request).join();
        assertThat(response.getSessionId())
                .isEqualTo(request.getSessionId());

        String nextNodeId = getNodeByName("DrinkType");

        assertThat(session.getData(Session.KEYS.CURRENT_NODE_ID.toString())).isEqualTo(nextNodeId);
        verify(sessionStorage, times(1)).updateSession(session);
        assertThat(response.getPrompt()).isEqualTo("Do you want a Beer or Tea?");
        assertThat(response.getType()).isEqualTo(Node.Type.CHOICE);
        assertThat(response.isLastResponse()).isFalse();
    }

    private String getNodeByName(String nodeName) {
        return Dummy.app
                .getNodes()
                .stream()
                .filter(n -> n.getName().equals(nodeName))
                .findFirst()
                .map(n -> n.getId())
                .orElse("");
    }

    @Test
    public void givenCurrentNodeIsChoice_WhenRequestHasChoiceInput_ReturnNextCorrectResponseBasedOnChoice () {
        Session session = new Session();
        session.putData(Session.KEYS.CURRENT_NODE_ID.toString(), getNodeByName("DrinkType"));
        session.setCallId(UUID.randomUUID().toString());

        Request request = Request.builder()
                .uri("/" + Dummy.APP_NAME)
                .sessionId(session.getCallId())
                .userInput("tea")
                .build();

        mockAppCallsToDummyApp();
        willReturn(CompletableFuture.completedFuture(session))
                .given(sessionStorage)
                .fetchSessionById(session.getCallId());

        Response response = server.handle(request).join();

        verify(sessionStorage, times(1)).updateSession(session);
        assertThat(response.getPrompt()).isEqualTo("Not a bad choice.");
        assertThat(response.getType()).isEqualTo(Node.Type.PROMPT);
        assertThat(response.isLastResponse()).isFalse();
    }

    @Test
    public void givenCurrentNodeIsLastButOne_WhenRequestHasNextArrives_ReturnLastResponse () {
        Session session = new Session();
        session.putData(Session.KEYS.CURRENT_NODE_ID.toString(), getNodeByName("Beer"));
        session.setCallId(UUID.randomUUID().toString());

        Request request = Request.builder()
                .uri("/" + Dummy.APP_NAME)
                .sessionId(session.getCallId())
                .build();

        mockAppCallsToDummyApp();
        willReturn(CompletableFuture.completedFuture(session))
                .given(sessionStorage)
                .fetchSessionById(session.getCallId());

        Response response = server.handle(request).join();

        verify(sessionStorage, times(1)).updateSession(session);
        assertThat(response.getType()).isEqualTo(Node.Type.PROMPT);
        assertThat(response.isLastResponse()).isTrue();
    }

    private void mockAppCallsToDummyApp() {
        willReturn(CompletableFuture.completedFuture(Dummy.app))
                .given(appStorage)
                .getApplicationByName(Dummy.APP_NAME);
    }
}
