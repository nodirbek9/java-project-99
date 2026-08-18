package hexlet.code.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskManagerOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Task Manager API")
                .version("0.0.1")
                .description("Система управления задачами: пользователи, статусы, задачи, метки"));
    }
}
