package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.entity.Label;
import hexlet.code.entity.Task;
import hexlet.code.entity.TaskStatus;
import hexlet.code.entity.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDataCleaner.class)
class TasksControllerTest {

    private static final int TEST_INDEX = 3140;
    private static final int NEW_INDEX = 12;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataCleaner cleaner;

    private User assignee;
    private TaskStatus draft;
    private Label label;
    private Task task;

    @BeforeEach
    void setUp() {
        cleaner.clean();

        var user = new User();
        user.setEmail("assignee@example.com");
        user.setFirstName("Ivan");
        user.setPassword(passwordEncoder.encode("secret"));
        assignee = userRepository.save(user);

        var status = new TaskStatus();
        status.setName("Draft");
        status.setSlug("draft");
        draft = taskStatusRepository.save(status);

        var reviewStatus = new TaskStatus();
        reviewStatus.setName("ToReview");
        reviewStatus.setSlug("to_review");
        taskStatusRepository.save(reviewStatus);

        var newLabel = new Label();
        newLabel.setName("bug");
        label = labelRepository.save(newLabel);

        var newTask = new Task();
        newTask.setName("Task 1");
        newTask.setDescription("Description of task 1");
        newTask.setIndex(TEST_INDEX);
        newTask.setTaskStatus(draft);
        newTask.setAssignee(assignee);
        newTask.setLabels(new HashSet<>(Set.of(label)));
        task = taskRepository.save(newTask);
    }

    @Test
    void testIndexWithoutAuthIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/tasks").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"));
    }

    @Test
    void testShow() throws Exception {
        mockMvc.perform(get("/api/tasks/" + task.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task 1"))
                .andExpect(jsonPath("$.content").value("Description of task 1"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.index").value(TEST_INDEX))
                .andExpect(jsonPath("$.assignee_id").value(assignee.getId()))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(label.getId()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testShowNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/999999").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var data = new HashMap<String, Object>();
        data.put("title", "Created task");
        data.put("content", "Created content");
        data.put("index", NEW_INDEX);
        data.put("status", "draft");
        data.put("assignee_id", assignee.getId());
        data.put("taskLabelIds", Set.of(label.getId()));

        mockMvc.perform(post("/api/tasks").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Created task"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.assignee_id").value(assignee.getId()))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(label.getId()));
    }

    @Test
    void testCreateWithUnknownLabelIsNotFound() throws Exception {
        var data = new HashMap<String, Object>();
        data.put("title", "Broken labels");
        data.put("status", "draft");
        data.put("taskLabelIds", Set.of(999999L));

        mockMvc.perform(post("/api/tasks").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateWithoutAuthIsUnauthorized() throws Exception {
        var data = Map.of("title", "Nope", "status", "draft");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateWithoutTitleIsBadRequest() throws Exception {
        var data = Map.of("content", "No title here", "status", "draft");

        mockMvc.perform(post("/api/tasks").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPartialUpdate() throws Exception {
        var data = Map.of("title", "Updated title", "content", "Updated content");

        mockMvc.perform(put("/api/tasks/" + task.getId()).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.content").value("Updated content"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.index").value(TEST_INDEX));
    }

    @Test
    void testUpdateStatusAndLabels() throws Exception {
        var data = Map.of("status", "to_review", "taskLabelIds", Set.of());

        mockMvc.perform(put("/api/tasks/" + task.getId()).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("to_review"))
                .andExpect(jsonPath("$.taskLabelIds").isEmpty());
    }

    @Test
    void testUpdateWithoutAuthIsUnauthorized() throws Exception {
        var data = Map.of("title", "Updated title");

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + task.getId()).with(jwt()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    void testDeleteAssignedUserIsConflict() throws Exception {
        mockMvc.perform(delete("/api/users/" + assignee.getId())
                        .with(jwt().jwt(b -> b.subject(assignee.getEmail()))))
                .andExpect(status().isConflict());

        assertThat(userRepository.findById(assignee.getId())).isPresent();
    }

    @Test
    void testFilterByTitleCont() throws Exception {
        mockMvc.perform(get("/api/tasks?titleCont=ask").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(task.getId()));

        mockMvc.perform(get("/api/tasks?titleCont=nothing-here").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testFilterByAssigneeId() throws Exception {
        mockMvc.perform(get("/api/tasks?assigneeId=" + assignee.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(task.getId()));
    }

    @Test
    void testFilterByLabelId() throws Exception {
        mockMvc.perform(get("/api/tasks?labelId=" + label.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(task.getId()));
    }

    @Test
    void testFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/tasks?status=draft").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(task.getId()));

        mockMvc.perform(get("/api/tasks?status=to_review").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testFilterByAllParams() throws Exception {
        var url = "/api/tasks?titleCont=Task"
                + "&assigneeId=" + assignee.getId()
                + "&status=" + draft.getSlug()
                + "&labelId=" + label.getId();

        mockMvc.perform(get(url).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(task.getId()));
    }
}
