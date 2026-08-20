package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Ограничения проверяются для значения внутри JsonNullable благодаря
 * {@link hexlet.code.util.JsonNullableValueExtractor}. Если поле не пришло в запросе,
 * оно не валидируется и не обновляется.
 */
@Getter
@Setter
public class UserUpdateDTO {

    private static final int MIN_PASSWORD_LENGTH = 3;

    private JsonNullable<String> firstName;

    private JsonNullable<String> lastName;

    @Email
    @NotBlank
    private JsonNullable<String> email;

    @NotBlank
    @Size(min = MIN_PASSWORD_LENGTH)
    private JsonNullable<String> password;
}
