package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.entity.User;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDataCleaner.class)
class AuthenticationControllerTest {

    private static final String ADMIN_EMAIL = "hexlet@example.com";
    private static final String ADMIN_PASSWORD = "qwerty";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataCleaner cleaner;

    @BeforeEach
    void setUp() {
        cleaner.clean();

        var admin = new User();
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        userRepository.save(admin);
    }

    @Test
    void testLoginWithValidCredentials() throws Exception {
        var data = Map.of("username", ADMIN_EMAIL, "password", ADMIN_PASSWORD);

        var result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn();

        var token = result.getResponse().getContentAsString();
        assertThat(token).isNotBlank();
        assertThat(token.split("[.]")).hasSize(3);
    }

    @Test
    void testLoginWithWrongPasswordIsUnauthorized() throws Exception {
        var data = Map.of("username", ADMIN_EMAIL, "password", "wrong-password");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginWithUnknownUserIsUnauthorized() throws Exception {
        var data = Map.of("username", "nobody@example.com", "password", ADMIN_PASSWORD);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginWithBlankCredentialsIsBadRequest() throws Exception {
        var data = Map.of("username", "", "password", "");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isBadRequest());
    }
}
