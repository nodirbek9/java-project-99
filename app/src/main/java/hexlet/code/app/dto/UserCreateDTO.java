package hexlet.code.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {

    private static final int MIN_PASSWORD_LENGTH = 3;

    private String firstName;

    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = MIN_PASSWORD_LENGTH)
    private String password;
}
