package hexlet.code.app;

import io.sentry.Sentry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "sentry.dsn=",
        "sentry.environment=test",
        "sentry.release=test-release"
})
class SentryConfigTest {

    @AfterEach
    void clearSentry() {
        Sentry.close();
    }

    @Test
    void contextStartsWithoutDsn() {
        assertThat(Sentry.getLastEventId()).isNull();
    }
}
