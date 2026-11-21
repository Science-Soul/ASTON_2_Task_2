package userservice.service;

import userservice.dto.UserRequestDto;
import userservice.dto.UserResponseDto;
import userservice.model.User;
import userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Пользователь с email " + dto.email() + " уже существует.");
        }
        User user = new User(dto.name(), dto.email(), dto.age());
        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + id + " не найден."));
        return mapToResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapToResponseDto)
            .toList();
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Пользователь с ID " + id + " не найден."));

        // Проверяем, не занят ли новый email другим пользователем
        if (!existingUser.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email " + dto.email() + " уже используется.");
        }

        existingUser.setName(dto.name());
        existingUser.setEmail(dto.email());
        existingUser.setAge(dto.age());

        User updatedUser = userRepository.save(existingUser);
        return mapToResponseDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Пользователь с ID " + id + " не найден.");
        }
        userRepository.deleteById(id);
    }

    // Маппер Entity -> DTO
    private UserResponseDto mapToResponseDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAge(),
            user.getCreatedAt()
        );
    }
}
