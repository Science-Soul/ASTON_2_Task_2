package dao;

import model.User;
import util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public void create(User user) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            session.persist(user);
            transaction.commit();

            logger.info("Пользователь успешно сохранен: {}", user.getEmail());
            System.out.println("🎉 Пользователь успешно создан!");
        } catch (Exception e) {
            if (transaction != null && transaction.isActive() && session != null && session.isOpen()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.debug("Не удалось откатить транзакцию", rollbackEx);
                }
            }

            boolean isDuplicate = e instanceof org.hibernate.exception.ConstraintViolationException
                || (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException)
                || e.toString().contains("ConstraintViolationException")
                || e.toString().contains("jakarta.persistence.EntityExistsException");

            if (isDuplicate) {
                logger.error("Ошибка сохранения: Пользователь с email {} уже существует!", user.getEmail());
                System.out.println("Ошибка: Пользователь с таким email уже зарегистрирован.");
            } else {
                logger.error("Критическая ошибка при создании пользователя", e);
                System.out.println("Не удалось создать пользователя. Ошибка базы данных.");
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.find(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User", User.class).list();
        } catch (Exception e) {
            logger.error("Ошибка при получении списка пользователей", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void update(User user) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();

            logger.info("Пользователь успешно обновлен: ID {}", user.getId());
            System.out.println("Данные пользователя обновлены!");
        } catch (Exception e) {
            if (transaction != null && transaction.isActive() && session != null && session.isOpen()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.debug("Не удалось откатить транзакцию обновления", rollbackEx);
                }
            }

            // Проверяем ошибку уникальности при обновлении
            boolean isDuplicate = e instanceof org.hibernate.exception.ConstraintViolationException
                || (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException)
                || e.toString().contains("ConstraintViolationException")
                || e.toString().contains("jakarta.persistence.EntityExistsException");

            if (isDuplicate) {
                logger.error("Ошибка обновления: Email '{}' уже занят другим пользователем!", user.getEmail());
                System.out.println("Ошибка: Этот email уже зарегистрирован у другого пользователя.");
            } else {
                logger.error("Критическая ошибка при обновлении пользователя с ID {}", user.getId(), e);
                System.out.println("Не удалось обновить данные. Ошибка базы данных.");
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.find(User.class, id);
            if (user != null) {
                session.remove(user);
                transaction.commit();
                logger.info("Пользователь с ID {} успешно удален", id);
            } else {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка при удалении пользователя с ID {}", id, e);
            throw e;
        }
    }
}
