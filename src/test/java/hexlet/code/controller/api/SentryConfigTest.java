package hexlet.code.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверяет, что контекст поднимается без настроенного DSN.
 * Локально и в CI Bugsink не подключён, и это не должно ломать приложение.
 */
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
