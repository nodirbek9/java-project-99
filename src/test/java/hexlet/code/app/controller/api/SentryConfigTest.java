package hexlet.code.app.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "sentry.dsn=",
        "sentry.environment=test",
        "sentry.release=test-release"
})
class SentryConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextStartsWithoutDsn() {
        assertThat(applicationContext).isNotNull();
    }
}