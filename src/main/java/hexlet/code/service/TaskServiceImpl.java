package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.entity.Task;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final TaskSpecification taskSpecification;

    @Override
    @Transactional(readOnly = true)
    public List<TaskDTO> getAll(TaskParamsDTO params) {
        var spec = taskSpecification.build(params);
        return taskRepository.findAll(spec).stream()
                .map(taskMapper::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDTO findById(Long id) {
        return taskMapper.map(find(id));
    }

    @Override
    @Transactional
    public TaskDTO create(TaskCreateDTO dto) {
        var task = taskMapper.map(dto);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    @Override
    @Transactional
    public TaskDTO update(Long id, TaskUpdateDTO dto) {
        var task = find(id);
        taskMapper.update(dto, task);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    @Override
    @Transactional
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
