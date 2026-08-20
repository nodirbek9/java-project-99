package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.entity.TaskStatus;
import hexlet.code.exception.ResourceInUseException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;

    private final TaskRepository taskRepository;

    private final TaskStatusMapper taskStatusMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TaskStatusDTO> getAll() {
        return taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatusDTO findById(Long id) {
        return taskStatusMapper.map(find(id));
    }

    @Override
    @Transactional
    public TaskStatusDTO create(TaskStatusCreateDTO dto) {
        var taskStatus = taskStatusMapper.map(dto);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    @Override
    @Transactional
    public TaskStatusDTO update(Long id, TaskStatusUpdateDTO dto) {
        var taskStatus = find(id);
        taskStatusMapper.update(dto, taskStatus);
        taskStatusRepository.save(taskStatus);
        return taskStatusMapper.map(taskStatus);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var taskStatus = find(id);
        if (taskRepository.existsByTaskStatusId(id)) {
            throw new ResourceInUseException("Task status is linked to tasks and cannot be deleted");
        }
        taskStatusRepository.delete(taskStatus);
    }

    private TaskStatus find(Long id) {
        return taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status with id " + id + " not found"));
    }
}
