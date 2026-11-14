import dao.UserDaoImpl;
import service.UserService;
import util.HibernateUtil;

import java.util.Scanner;

public class Main {
    private static final UserService userService = new UserService(new UserDaoImpl());

    public static void main(String[] args) {
        // Инициализируем пул соединений Hibernate при запуске
        HibernateUtil.getSessionFactory();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== USER SERVICE МЕНЮ ===");
                System.out.println("1. Создать пользователя");
                System.out.println("2. Найти пользователя по ID");
                System.out.println("3. Показать всех пользователей");
                System.out.println("4. Обновить пользователя");
                System.out.println("5. Удалить пользователя");
                System.out.println("6. Выйти");
                System.out.print("Выберите действие: ");

                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> createUser(scanner);
                        case "2" -> findUser(scanner);
                        case "3" -> showAllUsers();
                        case "4" -> updateUser(scanner);
                        case "5" -> deleteUser(scanner);
                        case "6" -> {
                            System.out.println("Завершение работы...");
                            return;
                        }
                        default -> System.out.println("Неверный пункт меню. Попробуйте еще раз.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка ввода: ожидалось целое число.");
                } catch (IllegalArgumentException e) {
                    // Перехватываем ошибки бизнес-логики и валидации из UserService
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    System.out.println("Произошла системная ошибка. Проверьте логи.");
                }
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void createUser(Scanner scanner) {
        System.out.print("Введите имя: ");
        String name = scanner.nextLine();

        System.out.print("Введите email: ");
        String email = scanner.nextLine();

        System.out.print("Введите возраст: ");
        int age = Integer.parseInt(scanner.nextLine().trim());

        userService.createUser(name, email, age);
    }

    private static void findUser(Scanner scanner) {
        System.out.print("Введите ID пользователя: ");
        Long id = Long.parseLong(scanner.nextLine().trim());

        userService.getUserById(id).ifPresentOrElse(
            System.out::println,
            () -> System.out.println("Пользователь с таким ID не найден.")
        );
    }

    private static void showAllUsers() {
        var users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Список пользователей пуст.");
        } else {
            System.out.println("--- Список всех пользователей ---");
            users.forEach(System.out::println);
        }
    }

    private static void updateUser(Scanner scanner) {
        System.out.print("Введите ID пользователя для обновления: ");
        Long id = Long.parseLong(scanner.nextLine().trim());

        System.out.print("Введите новое имя (оставьте пустым для пропуска): ");
        String name = scanner.nextLine();

        System.out.print("Введите новый email (оставьте пустым для пропуска): ");
        String email = scanner.nextLine();

        System.out.print("Введите новый возраст (или -1 для пропуска): ");
        int age = Integer.parseInt(scanner.nextLine().trim());

        // Сервис сам найдет пользователя, проверит новые данные и вызовет обновление
        userService.updateUser(id, name, email, age);
    }

    private static void deleteUser(Scanner scanner) {
        System.out.print("Введите ID пользователя для удаления: ");
        Long id = Long.parseLong(scanner.nextLine().trim());

        userService.deleteUser(id);
        System.out.println("🗑Операция удаления завершена.");
    }
}
