package runtime.micronaut;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.reactivex.Single;

import javax.validation.constraints.NotBlank;

@Client("/")
public interface HTTPControllerClient {
    @Get("/ivr/{appName}")
    Single<String> invokeApp(@NotBlank String appName);
}
