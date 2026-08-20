package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.entity.Task;
import hexlet.code.entity.TaskStatus;
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

import java.util.Map;

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
class TaskStatusesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestDataCleaner cleaner;

    private TaskStatus testStatus;

    @BeforeEach
    void setUp() {
        cleaner.clean();

        var status = new TaskStatus();
        status.setName("Draft");
        status.setSlug("draft");
        testStatus = taskStatusRepository.save(status);
    }

    @Test
    void testIndexWithoutAuthIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/task_statuses").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].slug").value("draft"));
    }

    @Test
    void testShow() throws Exception {
        mockMvc.perform(get("/api/task_statuses/" + testStatus.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Draft"))
                .andExpect(jsonPath("$.slug").value("draft"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testShowNotFound() throws Exception {
        mockMvc.perform(get("/api/task_statuses/999999").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var data = Map.of("name", "ToReview", "slug", "to_review");

        mockMvc.perform(post("/api/task_statuses").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("to_review"));

        assertThat(taskStatusRepository.findBySlug("to_review")).isPresent();
    }

    @Test
    void testCreateWithoutAuthIsUnauthorized() throws Exception {
        var data = Map.of("name", "Nope", "slug", "nope");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateWithBlankNameIsBadRequest() throws Exception {
        var data = Map.of("name", "", "slug", "blank-name");

        mockMvc.perform(post("/api/task_statuses").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPartialUpdate() throws Exception {
        var data = Map.of("name", "Renamed");

        mockMvc.perform(put("/api/task_statuses/" + testStatus.getId()).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.slug").value("draft"));
    }

    @Test
    void testUpdateWithoutAuthIsUnauthorized() throws Exception {
        var data = Map.of("name", "Renamed");

        mockMvc.perform(put("/api/task_statuses/" + testStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/" + testStatus.getId()).with(jwt()))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(testStatus.getId())).isEmpty();
    }

    @Test
    void testDeleteStatusInUseIsConflict() throws Exception {
        var task = new Task();
        task.setName("Linked task");
        task.setTaskStatus(testStatus);
        taskRepository.save(task);

        mockMvc.perform(delete("/api/task_statuses/" + testStatus.getId()).with(jwt()))
                .andExpect(status().isConflict());

        assertThat(taskStatusRepository.findById(testStatus.getId())).isPresent();
    }
}
