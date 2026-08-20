package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskStatusUpdateDTO {

    private static final int MIN_LENGTH = 1;

    @NotBlank
    @Size(min = MIN_LENGTH)
    private JsonNullable<String> name;

    @NotBlank
    @Size(min = MIN_LENGTH)
    private JsonNullable<String> slug;
}
