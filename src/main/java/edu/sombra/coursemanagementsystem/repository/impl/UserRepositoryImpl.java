package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.query.SqlQueryConstants;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.repository.base.impl.BaseRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean existsUserByEmail(String email) {
        Long count = entityManager().createQuery(SqlQueryConstants.EXIST_USER_BY_EMAIL_QUERY, Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return Optional.ofNullable(entityManager().createQuery(
                        SqlQueryConstants.FIND_USER_BY_EMAIL_QUERY, User.class)
                .setParameter("email", email)
                .getSingleResult());
    }

    @Override
    public void updateRoleByEmail(String email, RoleEnum role) {
        entityManager().createQuery(SqlQueryConstants.UPDATE_ROLE_BY_EMAIL_QUERY)
                .setParameter("role", role)
                .setParameter("email", email)
                .executeUpdate();
    }

    @Override
    public Optional<List<User>> findUsersByEmails(List<String> emails) {
        List<User> users = entityManager().createQuery(SqlQueryConstants.FIND_USERS_BY_EMAIL_QUERY, User.class)
                .setParameter("emails", emails)
                .getResultList();

        return Optional.ofNullable(users);
    }

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }
}
