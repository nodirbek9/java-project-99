package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceInUseException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 3;
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return userMapper.map(user);
    }

    public UserDTO create(UserCreateDTO dto) {
        var user = userMapper.map(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO update(Long id, UserUpdateDTO dto) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        var newPassword = dto.getPassword();
        if (newPassword != null && newPassword.isPresent()) {
            var value = newPassword.get();
            if (value == null || value.length() < MIN_PASSWORD_LENGTH) {
                throw new IllegalArgumentException("Password is too short");
            }
        }

        var newEmail = dto.getEmail();
        if (newEmail != null && newEmail.isPresent()) {
            var value = newEmail.get();
            if (value == null || !value.matches(EMAIL_PATTERN)) {
                throw new IllegalArgumentException("Email is not valid");
            }
        }

        userMapper.update(dto, user);

        if (newPassword != null && newPassword.isPresent()) {
            user.setPassword(passwordEncoder.encode(newPassword.get()));
        }

        userRepository.save(user);
        return userMapper.map(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User with id " + id + " not found");
        }
        if (taskRepository.existsByAssigneeId(id)) {
            throw new ResourceInUseException("User is assigned to tasks and cannot be deleted");
        }
        userRepository.deleteById(id);
    }
}
