package hexlet.code.app.service;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskParamsDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.entity.Task;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final TaskSpecification taskSpecification;

    public List<TaskDTO> getAll(TaskParamsDTO params) {
        var spec = taskSpecification.build(params);
        return taskRepository.findAll(spec).stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO findById(Long id) {
        return taskMapper.map(find(id));
    }

    public TaskDTO create(TaskCreateDTO dto) {
        var task = taskMapper.map(dto);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO update(Long id, TaskUpdateDTO dto) {
        var task = find(id);
        taskMapper.update(dto, task);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        var task = find(id);
        // связи в task_labels чистит Hibernate: Task владеющая сторона m2m
        taskRepository.delete(task);
    }

    private Task find(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
    }
}
