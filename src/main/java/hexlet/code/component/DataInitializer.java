package hexlet.code.component;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.entity.Label;
import hexlet.code.entity.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "hexlet@example.com";
    private static final String ADMIN_PASSWORD = "qwerty";

    private static final Map<String, String> DEFAULT_STATUSES = Map.of(
            "draft", "Draft",
            "to_review", "ToReview",
            "to_be_fixed", "ToBeFixed",
            "to_publish", "ToPublish",
            "published", "Published"
    );

    private static final List<String> DEFAULT_LABELS = List.of("feature", "bug");

    private final UserRepository userRepository;
    private final UserService userService;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;

    @Override
    public void run(ApplicationArguments args) {
        createAdmin();
        createDefaultStatuses();
        createDefaultLabels();
    }

    private void createAdmin() {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }
        var dto = new UserCreateDTO();
        dto.setEmail(ADMIN_EMAIL);
        dto.setPassword(ADMIN_PASSWORD);
        userService.create(dto);
    }

    private void createDefaultStatuses() {
        DEFAULT_STATUSES.forEach((slug, name) -> {
            if (taskStatusRepository.existsBySlug(slug)) {
                return;
            }
            var status = new TaskStatus();
            status.setSlug(slug);
            status.setName(name);
            taskStatusRepository.save(status);
        });
    }

    private void createDefaultLabels() {
        DEFAULT_LABELS.forEach(name -> {
            if (labelRepository.existsByName(name)) {
                return;
            }
            var label = new Label();
            label.setName(name);
            labelRepository.save(label);
        });
    }
}
