package userservice.dto;

import jakarta.validation.constraints.*;

public record UserRequestDto(
    @NotBlank(message = "Имя не может быть пустым")
    String name,

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    String email,

    @NotNull(message = "Возраст обязателен")
    @Min(value = 0, message = "Возраст не может быть отрицательным")
    @Max(value = 150, message = "Некорректный возраст")
    Integer age
) {}
