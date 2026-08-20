package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;

import java.util.List;

/**
 * Контракт работы с пользователями. Контроллеры зависят от этого интерфейса,
 * а не от конкретной реализации, так что реализацию можно заменить без правки контроллеров.
 */
public interface UserService {

    List<UserDTO> getAll();

    UserDTO findById(Long id);

    UserDTO create(UserCreateDTO dto);

    UserDTO update(Long id, UserUpdateDTO dto);

    void delete(Long id);
}
