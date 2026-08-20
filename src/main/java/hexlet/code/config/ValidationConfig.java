package hexlet.code.config;

import hexlet.code.util.JsonNullableValueExtractor;
import jakarta.validation.Configuration;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@org.springframework.context.annotation.Configuration
public class ValidationConfig {

    @Bean
    @Primary
    public LocalValidatorFactoryBean defaultValidator() {
        return new LocalValidatorFactoryBean() {
            @Override
            protected void postProcessConfiguration(Configuration<?> configuration) {
                if (configuration instanceof HibernateValidatorConfiguration hibernateConfiguration) {
                    hibernateConfiguration.addValueExtractor(new JsonNullableValueExtractor());
                }
            }
        };
    }
}
