package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseMark;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.CourseMarkRepository;
import edu.sombra.coursemanagementsystem.service.impl.CourseMarkServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseMarkServiceImplTest {

    @InjectMocks
    private CourseMarkServiceImpl courseMarkService;

    @Mock
    private CourseService courseService;

    @Mock
    private CourseMarkRepository courseMarkRepository;

    @Mock
    private UserService userService;

    private static Stream<List<CourseMark>> provideCourseMarkTestData() {
        return Stream.of(
                List.of(
                        new CourseMark(1L, BigDecimal.valueOf(85), new User(), new Course(), true),
                        new CourseMark(2L, BigDecimal.valueOf(99), new User(), new Course(), true),
                        new CourseMark(3L, BigDecimal.valueOf(60), new User(), new Course(), false)
                )
        );
    }

    private static Stream<Arguments> provideTestDataForSaveTotalMark() {
        return Stream.of(
                Arguments.of(1L, 2L, 90.0, true),
                Arguments.of(3L, 4L, 85.5, false)
        );
    }

    static class CourseMarkProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(1L, new CourseMark(1L, BigDecimal.valueOf(95), new User(), new Course(), true)),
                    Arguments.of(2L, new CourseMark(2L, BigDecimal.valueOf(85), new User(), new Course(), true)),
                    Arguments.of(3L, new CourseMark(3L, BigDecimal.valueOf(70), new User(), new Course(), false))
            );
        }
    }

    static Stream<CourseMark> courseMarkProvider() {
        return Stream.of(
                CourseMark.builder()
                        .id(10L)
                        .totalScore(BigDecimal.valueOf(95))
                        .user(new User())
                        .course(new Course())
                        .passed(true)
                        .build()
        );
    }

    static class IncorrectIdProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(100L),
                    Arguments.of(0L),
                    Arguments.of(-1L)
            );
        }
    }

    @ParameterizedTest
    @MethodSource("courseMarkProvider")
    void saveTest(CourseMark courseMark) {
        when(courseMarkRepository.save(any(CourseMark.class))).thenReturn(courseMark);
        when(courseMarkRepository.findById(courseMark.getId())).thenReturn(Optional.of(courseMark));

        courseMarkService.save(courseMark);

        CourseMark savedCourseMark = courseMarkService.findById(courseMark.getId());
        assertEquals(courseMark, savedCourseMark);
    }

    @ParameterizedTest
    @ArgumentsSource(CourseMarkProvider.class)
    void testFindById(Long id, CourseMark expectedCourseMark) {
        when(courseMarkRepository.findById(id)).thenReturn(Optional.ofNullable(expectedCourseMark));

        CourseMark result = courseMarkService.findById(id);

        assertEquals(expectedCourseMark, result);
    }


    @ParameterizedTest
    @ArgumentsSource(CourseMarkProvider.class)
    void testFindByIdWithCorrectId(Long id, CourseMark expectedCourseMark) {
        when(courseMarkRepository.findById(id)).thenReturn(Optional.ofNullable(expectedCourseMark));

        CourseMark result = courseMarkService.findById(id);

        assertEquals(expectedCourseMark, result);
    }

    @ParameterizedTest
    @ArgumentsSource(IncorrectIdProvider.class)
    void testFindByIdWithIncorrectId(Long id) {
        when(courseMarkRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            courseMarkService.findById(id);
        });
    }

    @ParameterizedTest
    @MethodSource("provideCourseMarkTestData")
    void testFindAll(List<CourseMark> expectedCourseMarks) {
        when(courseMarkRepository.findAll()).thenReturn(expectedCourseMarks);

        List<CourseMark> result = courseMarkService.findAll();

        assertEquals(expectedCourseMarks, result);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataForSaveTotalMark")
    void testSaveTotalMark(Long userId, Long courseId, Double averageMark, boolean isAllHomeworksGraded) {
        User user = new User();
        Course course = new Course();

        when(userService.findUserById(userId)).thenReturn(user);
        when(courseService.findById(courseId)).thenReturn(course);

        courseMarkService.saveTotalMark(userId, courseId, averageMark, isAllHomeworksGraded);

        verify(courseMarkRepository, times(1)).upsert(argThat(courseMark ->
                courseMark.getUser() == user &&
                        courseMark.getCourse() == course &&
                        courseMark.getTotalScore().equals(BigDecimal.valueOf(averageMark)) &&
                        courseMark.getPassed() == courseMarkService.isCoursePassed(averageMark, isAllHomeworksGraded)
        ));
    }

    @Test
    void testIsCoursePassed() {
        double passThreshold = 80.0;

        boolean result1 = courseMarkService.isCoursePassed(passThreshold, true);
        boolean result2 = courseMarkService.isCoursePassed(passThreshold, false);

        assertTrue(result1);
        assertFalse(result2);
    }
}
