package hexlet.code.util;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Учит Bean Validation заглядывать внутрь JsonNullable.
 * Без этого аннотации вида {@code @Email JsonNullable<String> email} молча не работают,
 * потому что валидатор видит контейнер, а не строку внутри него.
 * Отсутствующее значение не передаётся валидатору, так что частичное обновление работает как раньше.
 */
@UnwrapByDefault
public class JsonNullableValueExtractor implements ValueExtractor<JsonNullable<@ExtractedValue ?>> {

    @Override
    public void extractValues(JsonNullable<?> originalValue, ValueReceiver receiver) {
        if (originalValue != null && originalValue.isPresent()) {
            receiver.value(null, originalValue.get());
        }
    }
}
