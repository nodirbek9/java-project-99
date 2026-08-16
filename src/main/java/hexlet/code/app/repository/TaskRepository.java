package hexlet.code.app.repository;

import hexlet.code.app.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    boolean existsByAssigneeId(Long assigneeId);

    boolean existsByTaskStatusId(Long taskStatusId);

    boolean existsByLabelsId(Long labelId);
}
