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

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();
                Properties settings = new Properties();

                settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
                settings.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/user_db");
                settings.put("hibernate.connection.username", "postgres");
                settings.put("hibernate.connection.password", "0000");

                settings.put("hibernate.connection.pool_size", "5");
                settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                settings.put("hibernate.show_sql", "false");
                settings.put("hibernate.format_sql", "true");
                settings.put("hibernate.hbm2ddl.auto", "update");
                settings.put("hibernate.current_session_context_class", "thread");

                configuration.setProperties(settings);
                configuration.addAnnotatedClass(User.class);

                sessionFactory = configuration.buildSessionFactory();
                logger.info("SessionFactory успешно инициализирована.");
            } catch (Throwable ex) {
                logger.error("Критическая ошибка создания SessionFactory!", ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
        return sessionFactory;
    }

    // Метод для подмены фабрики в интеграционных тестах
    public static void setCustomSessionFactory(SessionFactory customSessionFactory) {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        sessionFactory = customSessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null;
            logger.info("SessionFactory успешно закрыта.");
        }
    }
}
