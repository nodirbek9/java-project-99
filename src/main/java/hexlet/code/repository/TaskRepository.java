package hexlet.code.repository;

import hexlet.code.entity.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Связанные сущности загружаются одним запросом вместе с самими задачами.
     * Без графа на список из N задач пришлось бы дополнительно дёргать базу на каждую связь.
     */
    @Override
    @EntityGraph(attributePaths = {"taskStatus", "assignee", "labels"})
    List<Task> findAll();

    @Override
    @EntityGraph(attributePaths = {"taskStatus", "assignee", "labels"})
    List<Task> findAll(Specification<Task> spec);

    @Override
    @EntityGraph(attributePaths = {"taskStatus", "assignee", "labels"})
    Optional<Task> findById(Long id);

    boolean existsByAssigneeId(Long assigneeId);

    boolean existsByTaskStatusId(Long taskStatusId);

    boolean existsByLabelsId(Long labelId);
}
