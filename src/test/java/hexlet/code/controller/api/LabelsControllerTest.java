package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.entity.Label;
import hexlet.code.entity.Task;
import hexlet.code.entity.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
class LabelsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TestDataCleaner cleaner;

    private Label testLabel;
    private TaskStatus testStatus;

    @BeforeEach
    void setUp() {
        cleaner.clean();

        var label = new Label();
        label.setName("bug");
        testLabel = labelRepository.save(label);

        var status = new TaskStatus();
        status.setName("Draft");
        status.setSlug("draft");
        testStatus = taskStatusRepository.save(status);
    }

    @Test
    void testIndexWithoutAuthIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/labels").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("bug"));
    }

    @Test
    void testShow() throws Exception {
        mockMvc.perform(get("/api/labels/" + testLabel.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bug"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testShowNotFound() throws Exception {
        mockMvc.perform(get("/api/labels/999999").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var data = Map.of("name", "feature");

        mockMvc.perform(post("/api/labels").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("feature"));

        assertThat(labelRepository.findByName("feature")).isPresent();
    }

    @Test
    void testCreateWithShortNameIsBadRequest() throws Exception {
        var data = Map.of("name", "ab");

        mockMvc.perform(post("/api/labels").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithoutAuthIsUnauthorized() throws Exception {
        var data = Map.of("name", "nope-label");

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdate() throws Exception {
        var data = Map.of("name", "critical-bug");

        mockMvc.perform(put("/api/labels/" + testLabel.getId()).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("critical-bug"));
    }

    @Test
    void testUpdateWithShortNameIsBadRequest() throws Exception {
        var data = Map.of("name", "ab");

        mockMvc.perform(put("/api/labels/" + testLabel.getId()).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/labels/" + testLabel.getId()).with(jwt()))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(testLabel.getId())).isEmpty();
    }

    @Test
    void testDeleteLabelInUseIsConflict() throws Exception {
        var task = new Task();
        task.setName("Linked task");
        task.setTaskStatus(testStatus);
        task.setLabels(new HashSet<>(Set.of(testLabel)));
        taskRepository.save(task);

        mockMvc.perform(delete("/api/labels/" + testLabel.getId()).with(jwt()))
                .andExpect(status().isConflict());

        assertThat(labelRepository.findById(testLabel.getId())).isPresent();
    }
}
