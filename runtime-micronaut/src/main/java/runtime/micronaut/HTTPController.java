package runtime.micronaut;

import com.experiment.ivr.core.core.exception.ApplicationNotFoundException;
import com.experiment.ivr.core.core.exception.SessionNotFoundException;
import com.experiment.ivr.usecase.ContinueCall;
import com.experiment.ivr.usecase.StartCall;
import com.experiment.ivr.usecase.impl.ContinueCallImpl;
import com.experiment.ivr.usecase.impl.StartCallImpl;
import com.experiment.ivr.usecase.model.Request;
import com.experiment.ivr.usecase.model.Response;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.annotation.Error;
import io.reactivex.Single;
import lombok.extern.flogger.Flogger;

import java.util.Optional;

@Controller("/ivr")
@Flogger
public class HTTPController {

    private StartCall newCall = new StartCallImpl();

    private ContinueCall existingCall = new ContinueCallImpl();

    @Post(
            uri = "/{appName}{?sessionId,userInput}",
            produces = MediaType.APPLICATION_XML
    )
    public Single<HttpResponse<?>> invokeApp(String appName,
                                          @QueryValue Optional<String> sessionId,
                                          @QueryValue Optional<String> userInput) {
        return sessionId
                .map(id -> this.handleExistingCall(appName, id,
                        userInput.orElse("")))
                .orElseGet(() -> this.handleNewCall(appName));
    }

    private Single<HttpResponse<?>> handleNewCall(String appName) {
        Request useCaseReq = Request.builder()
                .app(appName)
                .build();

        return Single.fromFuture(newCall.handle(useCaseReq)
                .thenApply(this::useCaseResponseToServerResponse)
        );
    }

    private Single<HttpResponse<?>> handleExistingCall(String appName,
                                              String id,
                                              String userInput) {
        Request useCaseReq = Request.builder()
                .app(appName)
                .sessionId(id)
                .userInput(userInput)
                .build();

        return Single.fromFuture(
                existingCall
                        .handle(useCaseReq)
                        .thenApply(this::useCaseResponseToServerResponse)
        );
    }

    private HttpResponse<?> useCaseResponseToServerResponse(Response response) {
        log.atInfo().log("Converting use case response to server response: %s", response);
        return HttpResponse
                .ok(response.getDocument())
                .header(HttpHeaders.LOCATION, response.getSessionId());
    }

    @Produces(MediaType.TEXT_HTML)
    @Error(exception = Throwable.class)
    public HttpResponse<?> onSavedFailed(HttpRequest request, Throwable ex) {
        Throwable cause = ex.getCause();
        if(cause instanceof SessionNotFoundException ||
                cause instanceof ApplicationNotFoundException) {
            return HttpResponse.notFound(cause);
        }
        return HttpResponse.serverError(ex.getMessage());
    }
}
