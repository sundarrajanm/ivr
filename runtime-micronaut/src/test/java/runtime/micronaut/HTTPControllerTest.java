package runtime.micronaut;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class HTTPControllerTest {
    private EmbeddedServer server;
//    private HTTPControllerClient client;
    private static RxHttpClient rxClient;

    @BeforeEach
    public void setup() {
        this.server = ApplicationContext.run(EmbeddedServer.class);
        this.rxClient = server.getApplicationContext()
                .createBean(RxHttpClient.class, server.getURL());
//        this.client = server.getApplicationContext().getBean(
//                HTTPControllerClient.class);
    }

    @Test
    public void shouldReturnHello() {
        HttpResponse response = rxClient.toBlocking().exchange(
                HttpRequest.POST("/ivr/dummy", "")
        );
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
    }

    @AfterEach
    public void cleanup() {
        this.server.stop();
    }
}
