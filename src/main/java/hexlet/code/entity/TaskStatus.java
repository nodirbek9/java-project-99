package hexlet.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "task_statuses")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class TaskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @CreatedDate
    private LocalDate createdAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> thisClass = effectiveClass(this);
        Class<?> otherClass = effectiveClass(o);
        if (thisClass != otherClass) {
            return false;
        }
        TaskStatus other = (TaskStatus) o;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public final int hashCode() {
        return effectiveClass(this).hashCode();
    }

    private static Class<?> effectiveClass(Object entity) {
        return entity instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : entity.getClass();
    }
}
