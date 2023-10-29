package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String EXIST_USER_BY_EMAIL_QUERY = "SELECT COUNT(u) FROM users u WHERE u.email = :email";

    private static final String FIND_USER_BY_EMAIL_QUERY = "SELECT u FROM users u WHERE email =: email";

    private static final String UPDATE_ROLE_BY_EMAIL_QUERY = "UPDATE users u SET u.role = :role WHERE u.email = :email";

    private static final String FIND_USERS_BY_EMAIL_QUERY = "SELECT u FROM users u WHERE u.email IN :emails";

    @Override
    public boolean existsUserByEmail(String email) {
        Long count = getEntityManager().createQuery(EXIST_USER_BY_EMAIL_QUERY, Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public User findUserByEmail(String email) {
        return getEntityManager().createQuery(FIND_USER_BY_EMAIL_QUERY, User.class)
                .setParameter("email", email)
                .getSingleResult();
    }

    @Override
    public void updateRoleByEmail(String email, RoleEnum role) {
        getEntityManager().createQuery(UPDATE_ROLE_BY_EMAIL_QUERY)
                .setParameter("role", role)
                .setParameter("email", email)
                .executeUpdate();
    }

    @Override
    public List<User> findUsersByEmails(List<String> emails) {
        return getEntityManager().createQuery(FIND_USERS_BY_EMAIL_QUERY, User.class)
                .setParameter("emails", emails)
                .getResultList();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }
}
