package hexlet.code.controller.api;

import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;

/**
 * Чистит базу перед каждым тестом, чтобы тесты не видели данные друг друга.
 * Порядок удаления важен: сначала задачи, потому что именно они ссылаются
 * на статусы, пользователей и метки.
 */
@TestComponent
@RequiredArgsConstructor
public class TestDataCleaner {

    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;

    @Transactional
    public void clean() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }
}
