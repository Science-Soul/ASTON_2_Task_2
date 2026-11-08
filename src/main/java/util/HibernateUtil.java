package util;

import model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory;

    static {
        try {
            System.setProperty("logback.configurationFile", "src/main/resources/logback.xml");

            Configuration configuration = new Configuration();

            Properties settings = new Properties();

            // Настройки драйвера и подключения к PostgreSQL
            settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            settings.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/user_db");
            settings.put("hibernate.connection.username", "postgres");
            settings.put("hibernate.connection.password", "0000");

            // Пул соединений и диалект
            settings.put("hibernate.connection.pool_size", "5");
            settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

            // Логирование SQL-запросов и автосоздание таблиц
            settings.put("hibernate.show_sql", "false");
            settings.put("hibernate.format_sql", "true");
            settings.put("hibernate.hbm2ddl.auto", "update");

            // Контекст сессий для текущего потока
            settings.put("hibernate.current_session_context_class", "thread");

            // Применяем настройки к конфигурации
            configuration.setProperties(settings);

            // Регистрируем наш класс-сущность User
            configuration.addAnnotatedClass(User.class);

            sessionFactory = configuration.buildSessionFactory();
            logger.info("SessionFactory успешно инициализирована через Java Properties.");
        } catch (Throwable ex) {
            logger.error("Критическая ошибка создания SessionFactory!", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("SessionFactory успешно закрыта.");
        }
    }
}
