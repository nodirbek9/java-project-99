package hexlet.code.app.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.entity.Task;
import hexlet.code.app.entity.TaskStatus;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class TaskStatusesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    private TaskStatus createStatus() {
        var suffix = String.valueOf(System.nanoTime());
        var status = new TaskStatus();
        status.setName("Status" + suffix);
        status.setSlug("status_" + suffix);
        return taskStatusRepository.save(status);
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
                .andExpect(jsonPath("$[0].slug").exists());
    }

    @Test
    void testShow() throws Exception {
        var status = createStatus();

        mockMvc.perform(get("/api/task_statuses/" + status.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(status.getName()))
                .andExpect(jsonPath("$.slug").value(status.getSlug()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testShowNotFound() throws Exception {
        mockMvc.perform(get("/api/task_statuses/999999").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var suffix = String.valueOf(System.nanoTime());
        var data = Map.of("name", "New" + suffix, "slug", "new_" + suffix);

        mockMvc.perform(post("/api/task_statuses").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("new_" + suffix));

        assertThat(taskStatusRepository.findBySlug("new_" + suffix)).isPresent();
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
        var data = Map.of("name", "", "slug", "blank_" + System.nanoTime());

        mockMvc.perform(post("/api/task_statuses").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPartialUpdate() throws Exception {
        var taskStatus = createStatus();
        var newName = "Renamed" + System.nanoTime();
        var data = Map.of("name", newName);

        mockMvc.perform(put("/api/task_statuses/" + taskStatus.getId()).with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.slug").value(taskStatus.getSlug()));
    }

    @Test
    void testDelete() throws Exception {
        var taskStatus = createStatus();

        mockMvc.perform(delete("/api/task_statuses/" + taskStatus.getId()).with(jwt()))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(taskStatus.getId())).isEmpty();
    }

    @Test
    void testDeleteStatusInUseIsConflict() throws Exception {
        var taskStatus = createStatus();
        var task = new Task();
        task.setName("Task " + System.nanoTime());
        task.setTaskStatus(taskStatus);
        taskRepository.save(task);

        mockMvc.perform(delete("/api/task_statuses/" + taskStatus.getId()).with(jwt()))
                .andExpect(status().isConflict());

        assertThat(taskStatusRepository.findById(taskStatus.getId())).isPresent();
    }
}
