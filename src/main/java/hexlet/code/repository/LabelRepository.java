package hexlet.code.repository;

import hexlet.code.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LabelRepository extends JpaRepository<Label, Long> {

    Optional<Label> findByName(String name);

    boolean existsByName(String name);

    /**
     * Забирает все метки одним запросом вместо обращения в базу на каждый идентификатор.
     */
    List<Label> findByIdIn(Set<Long> ids);
}
