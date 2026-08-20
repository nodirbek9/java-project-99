package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {

    private static final int MIN_TITLE_LENGTH = 1;

    @NotBlank
    @Size(min = MIN_TITLE_LENGTH)
    private JsonNullable<String> title;

    private JsonNullable<Integer> index;

    private JsonNullable<String> content;

    @NotBlank
    private JsonNullable<String> status;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    private JsonNullable<Set<Long>> taskLabelIds;
}
