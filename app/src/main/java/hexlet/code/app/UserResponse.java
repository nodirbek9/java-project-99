package hexlet.code.app;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String createdAt;
}
