package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.UpdateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.exception.LessonException;
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.mapper.LessonMapper;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.EnrollmentRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.LessonService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.LongStream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    public static final String FAILED_TO_DELETE_LESSON = "Failed to delete lesson";
    public static final String LESSON_DELETED_SUCCESSFULLY = "Lesson deleted successfully";
    public static final String COURSE_SHOULD_HAVE_AT_LEAST_5_LESSONS = "Course should have at least 5 lessons";
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonMapper lessonMapper;
    private final CourseMapper courseMapper;

    @Override
    public LessonResponseDTO save(CreateLessonDTO lessonDTO) {
        Course course = courseRepository.findById(lessonDTO.getCourseId())
                .orElseThrow(EntityNotFoundException::new);
        Lesson lesson = lessonRepository.save(Lesson.builder()
                .name(lessonDTO.getLessonName())
                .course(course)
                .build());
        CourseResponseDTO courseResponse = courseMapper.mapToResponseDTO(course);
        return lessonMapper.mapToResponseDTO(lesson, courseResponse);
    }

    @Override
    public LessonResponseDTO findById(Long lessonId, String userEmail) {
        User user = userRepository.findUserByEmail(userEmail);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(EntityNotFoundException::new);
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            CourseResponseDTO courseResponse = courseMapper.mapToResponseDTO(lesson.getCourse());
            return lessonMapper.mapToResponseDTO(lesson, courseResponse);
        } else {
            boolean isUserAssignedToCourse = enrollmentRepository.isUserAssignedToCourse(lesson.getCourse(), user);
            if (isUserAssignedToCourse) {
                CourseResponseDTO courseResponse = courseMapper.mapToResponseDTO(lesson.getCourse());
                return lessonMapper.mapToResponseDTO(lesson, courseResponse);
            } else {
                throw new IllegalArgumentException("User hasn't access to this lesson!");
            }
        }
    }

    @Override
    public List<LessonResponseDTO> findAllLessons(String userEmail) {
        User user = userRepository.findUserByEmail(userEmail);
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            List<Lesson> lessons = lessonRepository.findAll();
            List<CourseResponseDTO> courseResponseDTOS = lessons.stream()
                    .map(lesson -> courseMapper.mapToResponseDTO(lesson.getCourse()))
                    .toList();

            return lessonMapper.mapToResponsesDTO(lessons, courseResponseDTOS);
        } else {
            List<Lesson> lessons = lessonRepository.findAllLessonsByUserId(user.getId());
            List<CourseResponseDTO> courseResponseDTOS = lessons.stream()
                    .map(lesson -> courseMapper.mapToResponseDTO(lesson.getCourse()))
                    .toList();

            return lessonMapper.mapToResponsesDTO(lessons, courseResponseDTOS);
        }
    }

    @Override
    public List<LessonResponseDTO> findAllLessonsByCourse(Long courseId, String userEmail) {
        User user = userRepository.findUserByEmail(userEmail);
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            return findAllLessonsByCourse(courseId);
        } else {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(EntityNotFoundException::new);
            boolean isUserAssignedToCourse = enrollmentRepository.isUserAssignedToCourse(course, user);
            if (isUserAssignedToCourse) {
                return findAllLessonsByCourse(courseId);
            } else {
                throw new IllegalArgumentException("User hasn't access to this course!");
            }
        }
    }

    @Override
    public List<LessonResponseDTO> findAllLessonsByCourse(Long courseId) {
        List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
        List<CourseResponseDTO> courseResponseDTOS = lessons.stream()
                .map(lesson -> courseMapper.mapToResponseDTO(lesson.getCourse()))
                .toList();

        return lessonMapper.mapToResponsesDTO(lessons, courseResponseDTOS);
    }

    @Override
    public List<Lesson> generateAndAssignLessons(Long numberOfLessons, Course course) {
        if (numberOfLessons < 5) {
            throw new LessonException(COURSE_SHOULD_HAVE_AT_LEAST_5_LESSONS);
        }

        List<Lesson> generatedLessons = LongStream.rangeClosed(1, numberOfLessons)
                .mapToObj(i -> Lesson.builder()
                        .name("Lesson " + i)
                        .course(course)
                        .build())
                .toList();

        return lessonRepository.saveAll(generatedLessons);
    }

    @Override
    public void deleteLesson(Long id) {
        try {
            Lesson lesson = lessonRepository.findById(id)
                    .orElseThrow(EntityNotFoundException::new);
            lessonRepository.delete(lesson);
            log.info(LESSON_DELETED_SUCCESSFULLY);
        } catch (EntityDeletionException e) {
            log.error(FAILED_TO_DELETE_LESSON + id);
            throw new EntityDeletionException(FAILED_TO_DELETE_LESSON, e);
        }
    }

    //TODO: test it
    @Override
    public LessonResponseDTO editLesson(UpdateLessonDTO lesson) {
        lessonRepository.findById(lesson.getId());
        Course course = courseRepository.findById(lesson.getCourseId()).orElseThrow();
        Lesson editedLesson = Lesson.builder()
                .id(lesson.getId())
                .name(lesson.getName())
                .course(course)
                .build();
        Lesson updatedLesson = lessonRepository.update(editedLesson);
        CourseResponseDTO courseResponse = courseMapper.mapToResponseDTO(updatedLesson.getCourse());
        return lessonMapper.mapToResponseDTO(updatedLesson, courseResponse);
    }

    @Override
    public Lesson findLessonByHomeworkId(Long homeworkId) {
        return lessonRepository.findLessonByHomeworkId(homeworkId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
