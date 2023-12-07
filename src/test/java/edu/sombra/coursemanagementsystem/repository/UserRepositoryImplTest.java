package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class UserRepositoryImplTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testExistsUserByEmail_ExistingUser() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        boolean exists = userRepository.existsUserByEmail(user.getEmail());

        assertTrue(exists);
    }

    @Test
    void testExistsUserByEmail_NonExistingUser() {
        boolean exists = userRepository.existsUserByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void testFindUserByEmail_ExistingUser() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("user@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        User foundUser = userRepository.findUserByEmail(user.getEmail());

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void testFindUserByEmail_NonExistingUser() {
        assertThrows(NoResultException.class, () -> userRepository.findUserByEmail("nonexistent@example.com"));
    }

    @Test
    void testUpdateRoleByEmail() {
        User user = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("usertest@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.save(user);

        userRepository.updateRoleByEmail(user.getEmail(), RoleEnum.INSTRUCTOR);

        entityManager.flush();

        entityManager.clear();

        User updatedUser = userRepository.findUserByEmail(user.getEmail());

        assertEquals(RoleEnum.INSTRUCTOR, updatedUser.getRole());
    }

    @Test
    void testFindUsersByEmails() {
        User user1 = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("usertest1@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        User user2 = User.builder()
                .lastName("test")
                .firstName("user")
                .password("123")
                .email("usertest2@example.com")
                .role(RoleEnum.STUDENT)
                .build();

        userRepository.saveAll(Arrays.asList(user1, user2));

        List<User> foundUsers = userRepository.findUsersByEmails(Arrays.asList(user1.getEmail(), user2.getEmail()));

        assertEquals(2, foundUsers.size());
        assertTrue(foundUsers.contains(user1));
        assertTrue(foundUsers.contains(user2));
    }
}
