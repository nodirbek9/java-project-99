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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @CreatedDate
    private LocalDate createdAt;

    @LastModifiedDate
    private LocalDate updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Сравнение по идентификатору. Новая (ещё не сохранённая) сущность не равна никакой другой,
     * включая саму себя по значению, поэтому сравнение начинается с проверки ссылки.
     * Учитывается возможность прокси Hibernate: у прокси getClass() возвращает сгенерированный класс.
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
        User other = (User) o;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    /**
     * hashCode постоянен на протяжении жизни объекта. Идентификатор генерируется базой данных
     * и появляется только после сохранения, поэтому включать его в hashCode нельзя:
     * объект, положенный в HashSet до сохранения, стал бы недоступен после.
     */
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
