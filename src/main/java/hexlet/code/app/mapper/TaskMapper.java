package hexlet.code.app.mapper;

import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
import hexlet.code.app.entity.Label;
import hexlet.code.app.entity.Task;
import hexlet.code.app.entity.TaskStatus;
import hexlet.code.app.entity.User;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Task связан с тремя сущностями, а в DTO приезжают только идентификаторы и слаги.
 * Комбинация JsonNullable и разыменования ссылок в MapStruct читается хуже, чем явный маппер,
 * поэтому здесь обычный компонент.
 */
@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final TaskStatusRepository taskStatusRepository;

    private final UserRepository userRepository;

    private final LabelRepository labelRepository;

    public TaskDTO map(Task model) {
        var dto = new TaskDTO();
        dto.setId(model.getId());
        dto.setIndex(model.getIndex());
        dto.setTitle(model.getName());
        dto.setContent(model.getDescription());
        dto.setStatus(model.getTaskStatus() == null ? null : model.getTaskStatus().getSlug());
        dto.setAssigneeId(model.getAssignee() == null ? null : model.getAssignee().getId());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setTaskLabelIds(model.getLabels().stream()
                .map(Label::getId)
                .collect(Collectors.toCollection(HashSet::new)));
        return dto;
    }

    public Task map(TaskCreateDTO dto) {
        var model = new Task();
        model.setName(dto.getTitle());
        model.setIndex(dto.getIndex());
        model.setDescription(dto.getContent());
        model.setTaskStatus(resolveStatus(dto.getStatus()));
        model.setAssignee(resolveAssignee(dto.getAssigneeId()));
        model.setLabels(resolveLabels(dto.getTaskLabelIds()));
        return model;
    }

    public void update(TaskUpdateDTO dto, Task model) {
        if (isPresent(dto.getTitle())) {
            model.setName(dto.getTitle().get());
        }
        if (isPresent(dto.getIndex())) {
            model.setIndex(dto.getIndex().get());
        }
        if (isPresent(dto.getContent())) {
            model.setDescription(dto.getContent().get());
        }
        if (isPresent(dto.getStatus())) {
            model.setTaskStatus(resolveStatus(dto.getStatus().get()));
        }
        if (isPresent(dto.getAssigneeId())) {
            model.setAssignee(resolveAssignee(dto.getAssigneeId().get()));
        }
        if (isPresent(dto.getTaskLabelIds())) {
            model.setLabels(resolveLabels(dto.getTaskLabelIds().get()));
        }
    }

    private <T> boolean isPresent(JsonNullable<T> value) {
        return value != null && value.isPresent();
    }

    private TaskStatus resolveStatus(String slug) {
        if (slug == null) {
            throw new IllegalArgumentException("Task status is required");
        }
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Task status " + slug + " not found"));
    }

    private User resolveAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + assigneeId + " not found"));
    }

    private Set<Label> resolveLabels(Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return new HashSet<>();
        }
        var labels = new HashSet<Label>();
        for (var labelId : labelIds) {
            labels.add(labelRepository.findById(labelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Label with id " + labelId + " not found")));
        }
        return labels;
    }
}
