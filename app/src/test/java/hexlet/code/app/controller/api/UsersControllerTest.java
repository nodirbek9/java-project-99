package hexlet.code.app.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.entity.User;
import hexlet.code.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User anotherUser;

    private User createUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword(passwordEncoder.encode("secret"));
        return userRepository.save(user);
    }

    private MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder builder, User user) {
        return builder.with(jwt().jwt(b -> b.subject(user.getEmail())));
    }

    @BeforeEach
    void setUp() {
        var suffix = String.valueOf(System.nanoTime());
        testUser = createUser("john" + suffix + "@google.com");
        anotherUser = createUser("jack" + suffix + "@yahoo.com");
    }

    @Test
    void testIndexWithoutAuthIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testIndex() throws Exception {
        mockMvc.perform(asUser(get("/api/users"), testUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void testShow() throws Exception {
        mockMvc.perform(asUser(get("/api/users/" + testUser.getId()), testUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testShowNotFound() throws Exception {
        mockMvc.perform(asUser(get("/api/users/999999"), testUser))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreate() throws Exception {
        var email = "new" + System.nanoTime() + "@google.com";
        var data = Map.of(
                "email", email,
                "firstName", "Jack",
                "lastName", "Jons",
                "password", "some-password");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist());

        var saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("some-password");
        assertThat(passwordEncoder.matches("some-password", saved.getPassword())).isTrue();
    }

    @Test
    void testCreateWithInvalidEmailIsBadRequest() throws Exception {
        var data = Map.of("email", "not-an-email", "password", "some-password");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateWithShortPasswordIsBadRequest() throws Exception {
        var data = Map.of("email", "ok" + System.nanoTime() + "@google.com", "password", "ab");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPartialUpdateSelf() throws Exception {
        var newEmail = "updated" + System.nanoTime() + "@yahoo.com";
        var data = Map.of("email", newEmail, "password", "new-password");

        mockMvc.perform(asUser(put("/api/users/" + testUser.getId()), testUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(newEmail))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        var updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("new-password", updated.getPassword())).isTrue();
    }

    @Test
    void testUpdateAnotherUserIsForbidden() throws Exception {
        var data = Map.of("firstName", "Hacker");

        mockMvc.perform(asUser(put("/api/users/" + anotherUser.getId()), testUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteSelf() throws Exception {
        mockMvc.perform(asUser(delete("/api/users/" + testUser.getId()), testUser))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

    @Test
    void testDeleteAnotherUserIsForbidden() throws Exception {
        mockMvc.perform(asUser(delete("/api/users/" + anotherUser.getId()), testUser))
                .andExpect(status().isForbidden());
    }
}
