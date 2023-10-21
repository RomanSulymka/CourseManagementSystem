package edu.sombra.coursemanagementsystem.repository.impl;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.query.SqlQueryConstants;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
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

    private static final String GET_COURSE_BY_USER_ID_AND_COURSE_ID = "SELECT c FROM courses c " +
            "INNER JOIN enrollments e on c.id = e.course.id INNER JOIN users u on u.id = e.user.id " +
            "WHERE u.id =: userId AND c.id =: courseId";

    @Override
    public boolean existsUserByEmail(String email) {
        Long count = getEntityManager().createQuery(SqlQueryConstants.EXIST_USER_BY_EMAIL_QUERY, Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public User findUserByEmail(String email) {
        return getEntityManager().createQuery(
                        SqlQueryConstants.FIND_USER_BY_EMAIL_QUERY, User.class)
                .setParameter("email", email)
                .getSingleResult();
    }

    @Override
    public void updateRoleByEmail(String email, RoleEnum role) {
        getEntityManager().createQuery(SqlQueryConstants.UPDATE_ROLE_BY_EMAIL_QUERY)
                .setParameter("role", role)
                .setParameter("email", email)
                .executeUpdate();
    }

    @Override
    public Optional<List<User>> findUsersByEmails(List<String> emails) {
        List<User> users = getEntityManager().createQuery(SqlQueryConstants.FIND_USERS_BY_EMAIL_QUERY, User.class)
                .setParameter("emails", emails)
                .getResultList();

        return Optional.ofNullable(users);
    }

    @Override
    public boolean isInstructorAssignedToCourse(Long instructorId, String courseId) {
        try {
            Course course = getEntityManager().createQuery(GET_COURSE_BY_USER_ID_AND_COURSE_ID, Course.class)
                    .setParameter("userId", instructorId)
                    .setParameter("courseId", courseId)
                    .getSingleResult();

            return course != null;
        } catch (NoResultException e) {
            return false;
        }
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
