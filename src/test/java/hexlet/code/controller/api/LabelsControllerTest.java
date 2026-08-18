package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.entity.Label;
import hexlet.code.entity.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    private Label createLabel() {
        var label = new Label();
        label.setName("label-" + System.nanoTime());
        return labelRepository.save(label);
    }

    @Test
    void testIndexWithoutAuthIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDefaultLabelsExist() throws Exception {
        assertThat(labelRepository.findByName("feature")).isPresent();
        assertThat(labelRepository.findByName("bug")).isPresent();
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/labels").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void testShow() throws Exception {
        var label = createLabel();

        mockMvc.perform(get("/api/labels/" + label.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(label.getName()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testCreate() throws Exception {
        var name = "new-label-" + System.nanoTime();
        var data = Map.of("name", name);

        mockMvc.perform(post("/api/labels").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name));

        assertThat(labelRepository.findByName(name)).isPresent();
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
        var label = createLabel();
        var newName = "updated-label-" + System.nanoTime();

        mockMvc.perform(put("/api/labels/" + label.getId()).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", newName))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName));
    }

    @Test
    void testDelete() throws Exception {
        var label = createLabel();

        mockMvc.perform(delete("/api/labels/" + label.getId()).with(jwt()))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(label.getId())).isEmpty();
    }

    @Test
    void testDeleteLabelInUseIsConflict() throws Exception {
        var label = createLabel();
        var task = new Task();
        task.setName("Task " + System.nanoTime());
        task.setTaskStatus(taskStatusRepository.findBySlug("draft").orElseThrow());
        task.setLabels(new HashSet<>(Set.of(label)));
        taskRepository.save(task);

        mockMvc.perform(delete("/api/labels/" + label.getId()).with(jwt()))
                .andExpect(status().isConflict());

        assertThat(labelRepository.findById(label.getId())).isPresent();
    }
}
