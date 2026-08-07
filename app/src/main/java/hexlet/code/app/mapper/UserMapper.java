package hexlet.code.app.mapper;

import hexlet.code.app.UserResponse;
import hexlet.code.app.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
