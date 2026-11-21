package userservice.controller;

import userservice.dto.UserRequestDto;
import userservice.dto.UserResponseDto;
import userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(userservice.controller.UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/users - Успешное создание пользователя (201 Created)")
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        UserRequestDto request = new UserRequestDto("Иван", "ivan@test.com", 25);
        UserResponseDto response = new UserResponseDto(1L, "Иван", "ivan@test.com", 25, LocalDateTime.now());

        when(userService.createUser(any(UserRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Иван"))
            .andExpect(jsonPath("$.email").value("ivan@test.com"));
    }

    @Test
    @DisplayName("POST /api/users - Ошибка валидации при пустом имени и плохом email (400 Bad Request)")
    void createUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserRequestDto invalidRequest = new UserRequestDto("", "not-an-email", -5);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.age").exists());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GET /api/users/{id} - Получение пользователя по ID (200 OK)")
    void getUserById_ExistingId_ReturnsUser() throws Exception {
        UserResponseDto response = new UserResponseDto(1L, "Анна", "anna@test.com", 30, LocalDateTime.now());
        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Анна"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Пользователь не найден (404 Not Found)")
    void getUserById_NonExistingId_ReturnsNotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new EntityNotFoundException("Пользователь с ID 99 не найден."));

        mockMvc.perform(get("/api/users/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Пользователь с ID 99 не найден."));
    }

    @Test
    @DisplayName("GET /api/users - Получение списка всех пользователей (200 OK)")
    void getAllUsers_ReturnsList() throws Exception {
        List<UserResponseDto> users = List.of(
            new UserResponseDto(1L, "User1", "u1@test.com", 20, LocalDateTime.now()),
            new UserResponseDto(2L, "User2", "u2@test.com", 22, LocalDateTime.now())
        );
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Обновление пользователя (200 OK)")
    void updateUser_ValidRequest_ReturnsUpdatedUser() throws Exception {
        UserRequestDto updateRequest = new UserRequestDto("Новое Имя", "new@test.com", 26);
        UserResponseDto updatedResponse = new UserResponseDto(1L, "Новое Имя", "new@test.com", 26, LocalDateTime.now());

        when(userService.updateUser(eq(1L), any(UserRequestDto.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Новое Имя"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Удаление пользователя (204 No Content)")
    void deleteUser_ExistingId_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("POST /api/users - Ошибка бизнес-логики, например дубликат email (400 Bad Request)")
    void createUser_IllegalArgument_ReturnsBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto("Иван", "duplicate@test.com", 25);
        when(userService.createUser(any(UserRequestDto.class)))
            .thenThrow(new IllegalArgumentException("Email уже используется"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Email уже используется"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Ошибка валидации при обновлении (400 Bad Request)")
    void updateUser_InvalidRequest_ReturnsBadRequest() throws Exception {
        UserRequestDto invalidRequest = new UserRequestDto("", "bad-email", -1);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.age").exists());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Обновление несуществующего пользователя (404 Not Found)")
    void updateUser_NonExistingId_ReturnsNotFound() throws Exception {
        UserRequestDto request = new UserRequestDto("Новое Имя", "new@test.com", 26);
        when(userService.updateUser(eq(99L), any(UserRequestDto.class)))
            .thenThrow(new EntityNotFoundException("Пользователь с ID 99 не найден."));

        mockMvc.perform(put("/api/users/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Пользователь с ID 99 не найден."));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Удаление несуществующего пользователя (404 Not Found)")
    void deleteUser_NonExistingId_ReturnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Пользователь с ID 99 не найден."))
            .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Пользователь с ID 99 не найден."));
    }
}
