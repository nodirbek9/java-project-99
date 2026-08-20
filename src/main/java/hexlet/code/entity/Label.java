package hexlet.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "labels")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "labels")
    private Set<Task> tasks = new HashSet<>();

    @CreatedDate
    private LocalDate createdAt;

    /**
     * Метки хранятся в HashSet внутри Task, поэтому корректные equals и hashCode обязательны.
     * Коллекция tasks в сравнении не участвует: иначе получится бесконечная рекурсия
     * Task -> Label -> Task и подгрузка всего графа объектов из базы.
     */
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
        Label other = (Label) o;
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
