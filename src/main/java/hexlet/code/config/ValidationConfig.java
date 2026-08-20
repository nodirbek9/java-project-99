package hexlet.code.config;

import hexlet.code.util.JsonNullableValueExtractor;
import jakarta.validation.Configuration;
import jakarta.validation.valueextraction.ValueExtractor;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@org.springframework.context.annotation.Configuration
public class ValidationConfig {

    public static class JsonNullableValueExtractor
            implements ValueExtractor<JsonNullable<?>> {

        @Override
        public void extractValues(JsonNullable<?> originalValue,
                                  ValueReceiver receiver) {
            if (originalValue != null && originalValue.isPresent()) {
                receiver.value(null, originalValue.get());
            }
        }
    }
}
