package dao;

import model.User;
import util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoImplTest {
    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("test_user_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    private static SessionFactory testSessionFactory;
    private UserDao userDao;

    @BeforeAll
    static void initHibernate() {
        Configuration configuration = new Configuration();
        Properties settings = new Properties();

        // Берем динамический URL, логин и пароль прямо из запущенного контейнера
        settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        settings.put("hibernate.connection.url", postgresContainer.getJdbcUrl());
        settings.put("hibernate.connection.username", postgresContainer.getUsername());
        settings.put("hibernate.connection.password", postgresContainer.getPassword());

        settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        settings.put("hibernate.hbm2ddl.auto", "create-drop"); // Создаем таблицы при старте тестов
        settings.put("hibernate.current_session_context_class", "thread");
        settings.put("hibernate.show_sql", "false");

        configuration.setProperties(settings);
        configuration.addAnnotatedClass(User.class);

        testSessionFactory = configuration.buildSessionFactory();
        // Подменяем основную фабрику сессий на тестовую
        HibernateUtil.setCustomSessionFactory(testSessionFactory);
    }

    @AfterAll
    static void closeHibernate() {
        HibernateUtil.shutdown();
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
        // Изоляция тестов: очищаем таблицу перед каждым тестом и сбрасываем счетчик ID
        try (Session session = testSessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createNativeMutationQuery("TRUNCATE TABLE users RESTART IDENTITY").executeUpdate();
            tx.commit();
        }
    }

    @Test
    @DisplayName("Create & Read: Сохранение пользователя в реальный PostgreSQL и поиск по ID")
    void createAndFindById_Success() {
        User user = new User("Алексей", "alex@postgres.ru", 28);

        userDao.create(user);

        assertNotNull(user.getId(), "База данных должна присвоить ID");
        assertNotNull(user.getCreatedAt(), "Поле created_at должно заполниться автоматически");

        Optional<User> found = userDao.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("Алексей", found.get().getName());
        assertEquals("alex@postgres.ru", found.get().getEmail());
    }

    @Test
    @DisplayName("Read All: Получение списка всех пользователей")
    void findAll_ReturnsAllSavedUsers() {
        userDao.create(new User("Иван", "ivan@postgres.ru", 22));
        userDao.create(new User("Елена", "elena@postgres.ru", 25));

        List<User> users = userDao.findAll();

        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Update: Обновление данных существующего пользователя")
    void update_ModifiesExistingUser() {
        User user = new User("Дмитрий", "dima@postgres.ru", 30);
        userDao.create(user);

        user.setName("Дмитрий Обновленный");
        user.setAge(31);
        userDao.update(user);

        Optional<User> updated = userDao.findById(user.getId());
        assertTrue(updated.isPresent());
        assertEquals("Дмитрий Обновленный", updated.get().getName());
        assertEquals(31, updated.get().getAge());
    }

    @Test
    @DisplayName("Delete: Удаление пользователя из БД")
    void delete_RemovesUserFromDb() {
        User user = new User("Ольга", "olga@postgres.ru", 27);
        userDao.create(user);
        Long id = user.getId();

        userDao.delete(id);

        Optional<User> deleted = userDao.findById(id);
        assertTrue(deleted.isEmpty(), "Пользователь должен отсутствовать в БД после удаления");
    }

    @Test
    @DisplayName("Constraint Violation: Попытка сохранить дубликат email не роняет приложение и не создает дубль")
    void create_DuplicateEmail_HandledGracefully() {
        userDao.create(new User("Первый", "same@postgres.ru", 20));

        // Внутри UserDaoImpl ошибка перехватывается, логируется, а транзакция откатывается
        assertDoesNotThrow(() -> userDao.create(new User("Второй", "same@postgres.ru", 25)));

        // Проверяем, что в базе осталась только первая запись
        List<User> users = userDao.findAll();
        assertEquals(1, users.size());
        assertEquals("Первый", users.get(0).getName());
    }
}
