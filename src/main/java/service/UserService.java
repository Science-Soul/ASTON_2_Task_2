package service;

import dao.UserDao;
import model.User;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;

    // Внедряем DAO через конструктор, чтобы легко подменять его на Mock в тестах
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void createUser(String name, String email, int age) {
        validateUserData(name, email, age);
        User user = new User(name.trim(), email.trim(), age);
        userDao.create(user);
    }

    public Optional<User> getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным числом.");
        }
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public void updateUser(Long id, String newName, String newEmail, Integer newAge) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Некорректный ID пользователя.");
        }

        User existingUser = userDao.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + id + " не найден."));

        if (newName != null && !newName.isBlank()) {
            existingUser.setName(newName.trim());
        }
        if (newEmail != null && !newEmail.isBlank()) {
            existingUser.setEmail(newEmail.trim());
        }
        if (newAge != null && newAge != -1) {
            if (newAge < 0 || newAge > 150) {
                throw new IllegalArgumentException("Возраст должен быть от 0 до 150 лет.");
            }
            existingUser.setAge(newAge);
        }

        userDao.update(existingUser);
    }

    public void deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным числом.");
        }
        userDao.delete(id);
    }

    private void validateUserData(String name, String email, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Указан некорректный email.");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Возраст должен быть в диапазоне от 0 до 150.");
        }
    }
}
