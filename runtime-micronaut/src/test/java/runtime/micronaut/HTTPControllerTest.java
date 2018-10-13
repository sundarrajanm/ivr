package runtime.micronaut;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class HTTPControllerTest {
    private EmbeddedServer server;
    private HTTPControllerClient client;

    @BeforeEach
    public void setup() {
        this.server = ApplicationContext.run(EmbeddedServer.class);
        this.client = server.getApplicationContext().getBean(
                HTTPControllerClient.class);
    }

    @Test
    public void shouldReturnHello() {
        String response = client.invokeApp("Jonas")
                .blockingGet();
        assertThat(response).isEqualTo("Hello Jonas!");
    }

    @AfterEach
    public void cleanup() {
        this.server.stop();
    }
}
