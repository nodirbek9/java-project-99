package hexlet.code.app.service;

import hexlet.code.app.UserResponse;
import hexlet.code.app.entity.User;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private final UserMapper userMapper;


    public UserResponse getOne(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("User not found")
        );
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
