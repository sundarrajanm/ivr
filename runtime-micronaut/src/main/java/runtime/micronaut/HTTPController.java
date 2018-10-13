package runtime.micronaut;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.reactivex.Single;

@Controller("/ivr")
public class HTTPController {

    @Get("/{appName}")
    public Single<String> invokeApp(String appName) {
        return Single.just("Hello " + appName + "!");
    }
}
