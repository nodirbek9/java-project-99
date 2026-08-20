package hexlet.code.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class LabelUpdateDTO {

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 1000;

    @NotBlank
    @Size(min = MIN_LENGTH, max = MAX_LENGTH)
    private JsonNullable<String> name;
}
