package hexlet.code.config;

import io.sentry.Sentry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfig {

    public SentryConfig(
            @Value("${sentry.environment:development}") String environment,
            @Value("${sentry.release:java-project-99@dev}") String release) {
        Sentry.configureScope(scope -> {
            scope.setTag("application", "java-project-99");
            scope.setTag("environment", environment);
            scope.setTag("release", release);
        });
    }
}
