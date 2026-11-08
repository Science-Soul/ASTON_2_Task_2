import dao.UserDao;
import dao.UserDaoImpl;
import model.User;
import util.HibernateUtil;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final UserDao userDao = new UserDaoImpl();

    public static void main(String[] args) {
        // Инициализируем Hibernate при старте приложения
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
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void createUser(Scanner scanner) {
        System.out.print("Введите имя: ");
        String name = scanner.nextLine().trim();
        if (name.isBlank()) {
            System.out.println("Имя не может быть пустым!");
            return;
        }

        System.out.print("Введите email: ");
        String email = scanner.nextLine().trim();
        if (email.isBlank()) {
            System.out.println("Email не может быть пустым!");
            return;
        }

        System.out.print("Введите возраст: ");
        int age;
        try {
            // Защищаем парсинг от случайных букв, пробелов или пустых строк
            age = Integer.parseInt(scanner.nextLine().trim());
            if (age < 0 || age > 150) {
                System.out.println("Возраст должен быть в диапазоне от 0 до 150!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка ввода: Возраст должен быть целым числом!");
            return; // Возвращаемся в меню, не ломая программу
        }

        // Если все проверки пройдены, отправляем объект в DAO
        User user = new User(name, email, age);
        userDao.create(user);
    }


    private static void findUser(Scanner scanner) {
        System.out.print("Введите ID пользователя: ");
        Long id = Long.parseLong(scanner.nextLine());

        userDao.findById(id).ifPresentOrElse(
            System.out::println,
            () -> System.out.println("Пользователь с таким ID не найден.")
        );
    }

    private static void showAllUsers() {
        List<User> users = userDao.findAll();
        if (users.isEmpty()) {
            System.out.println("Список пользователей пуст.");
        } else {
            System.out.println("--- Список всех пользователей ---");
            users.forEach(System.out::println);
        }
    }

    private static void updateUser(Scanner scanner) {
        System.out.print("Введите ID пользователя для обновления: ");
        Long id = Long.parseLong(scanner.nextLine());

        userDao.findById(id).ifPresentOrElse(user -> {
            System.out.print("Введите новое имя (оставьте пустым для пропуска): ");
            String name = scanner.nextLine();
            if (!name.isBlank()) user.setName(name);

            System.out.print("Введите новый email (оставьте пустым для пропуска): ");
            String email = scanner.nextLine();
            if (!email.isBlank()) user.setEmail(email);

            System.out.print("Введите новый возраст (или -1 для пропуска): ");
            int age = Integer.parseInt(scanner.nextLine());
            if (age != -1) user.setAge(age);

            userDao.update(user);
            System.out.println("Данные пользователя обновлены!");
        }, () -> System.out.println("Пользователь с таким ID не найден."));
    }

    private static void deleteUser(Scanner scanner) {
        System.out.print("Введите ID пользователя для удаления: ");
        Long id = Long.parseLong(scanner.nextLine());
        userDao.delete(id);
        System.out.println("🗑Операция удаления завершена.");
    }
}
