package service;

import dao.UserDao;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao; // Имитация базы данных

    @InjectMocks
    private UserService userService; // Тестируемый сервис, куда автоматически внедряется мок

    /*@Test
    @DisplayName("Успешное создание пользователя с валидными данными")
    void createUser_ValidData_CallsDaoCreate() {
        // Act (Действие)
        userService.createUser("Иван", "ivan@test.com", 25);

        // Assert (Проверка): убеждаемся, что метод create у DAO был вызван ровно 1 раз
        verify(userDao, times(1)).create(any(User.class));
    }*/

    @Test
    @DisplayName("Успешное создание пользователя с валидными данными")
    void createUser_ValidData_CallsDaoCreate() {
        // Act
        userService.createUser("Иван", "ivan@test.com", 25);

        // Assert: перехватываем объект, который сервис передал в DAO
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao, times(1)).create(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("Иван", savedUser.getName());
        assertEquals("ivan@test.com", savedUser.getEmail());
        assertEquals(25, savedUser.getAge());
    }


    @Test
    @DisplayName("Ошибка при попытке создать пользователя с некорректным email")
    void createUser_InvalidEmail_ThrowsExceptionAndDoesNotCallDao() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            userService.createUser("Иван", "invalid-email", 25)
        );

        // Убеждаемся, что до базы данных запрос даже не дошел
        verify(userDao, never()).create(any());
    }

    @Test
    @DisplayName("Ошибка при некорректном возрасте")
    void createUser_NegativeAge_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            userService.createUser("Иван", "ivan@test.com", -5)
        );
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("Успешный поиск пользователя по ID")
    void getUserById_ExistingId_ReturnsUser() {
        // Arrange (Подготовка данных мока)
        User mockUser = new User("Анна", "anna@test.com", 30);
        mockUser.setId(1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Анна", result.get().getName());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Получение списка всех пользователей")
    void getAllUsers_ReturnsUserList() {
        // Arrange
        List<User> mockList = List.of(
            new User("User1", "u1@test.com", 20),
            new User("User2", "u2@test.com", 22)
        );
        when(userDao.findAll()).thenReturn(mockList);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    @DisplayName("Обновление существующего пользователя")
    void updateUser_ExistingUser_UpdatesFieldsAndCallsDao() {
        // Arrange
        User existingUser = new User("Старое Имя", "old@test.com", 20);
        existingUser.setId(1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));

        // Act
        userService.updateUser(1L, "Новое Имя", "", -1);

        // Assert
        assertEquals("Новое Имя", existingUser.getName());
        assertEquals("old@test.com", existingUser.getEmail()); // Почта не должна была измениться
        verify(userDao, times(1)).update(existingUser);
    }

    @Test
    @DisplayName("Ошибка при попытке обновить несуществующего пользователя")
    void updateUser_NonExistingId_ThrowsException() {
        when(userDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            userService.updateUser(99L, "Имя", "mail@test.com", 25)
        );

        verify(userDao, never()).update(any());
    }

    @Test
    @DisplayName("Успешное удаление пользователя по ID")
    void deleteUser_ValidId_CallsDaoDelete() {
        userService.deleteUser(1L);
        verify(userDao, times(1)).delete(1L);
    }
}
