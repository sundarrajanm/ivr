package com.experiment.ivr.dataprovider;

import com.experiment.ivr.dataprovider.storage.Dummy;
import com.experiment.ivr.vxml.VXMLServer;
import com.experiment.ivr.vxml.exception.ApplicationNotFoundException;
import com.experiment.ivr.vxml.model.Request;
import com.experiment.ivr.vxml.model.Response;
import com.experiment.ivr.vxml.model.Session;
import com.experiment.ivr.vxml.storage.AppStorage;
import com.experiment.ivr.vxml.storage.SessionStorage;
import com.experiment.ivr.vxml.utils.FutureUtil;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.willReturn;

@ExtendWith(MockitoExtension.class)
public class DummyAppTest {

    @Mock
    private AppStorage appStorage;

    @Mock
    private SessionStorage sessionStorage;

    @InjectMocks
    private VXMLServer server;

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
    public void startKnownApplication_ShouldReturnFirstVXMLPage () {

        Session session = new Session();
        session.setCallId(UUID.randomUUID().toString());
        willReturn(CompletableFuture.completedFuture(Dummy.app))
                .given(appStorage)
                .getApplicationByName(Dummy.APP_NAME);
        willReturn(CompletableFuture.completedFuture(session))
                .given(sessionStorage)
                .createNewSessionWithId();

        Request request = Request.builder()
                .uri("/" + Dummy.APP_NAME)
                .build();

        Response response = server.handle(request).join();
        assertNotNull(response, "Response was null");
    }

    @Test
    public void continueKnownApplication_ShouldReturnSubsequentVXMLPage () {

        Session session = new Session();
        session.setCallId(UUID.randomUUID().toString());
        Request request = Request.builder()
                .uri("/" + Dummy.APP_NAME)
                .sessionId(session.getCallId())
                .build();

        willReturn(CompletableFuture.completedFuture(Dummy.app))
                .given(appStorage)
                .getApplicationByName(Dummy.APP_NAME);
        willReturn(CompletableFuture.completedFuture(session))
                .given(sessionStorage)
                .fetchSessionById(session.getCallId());

        Response response = server.handle(request).join();
        assertThat(response.getSessionId())
                .isEqualTo(request.getSessionId());
    }
}
