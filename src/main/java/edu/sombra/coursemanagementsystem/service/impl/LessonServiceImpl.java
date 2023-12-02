package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.UpdateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.exception.LessonException;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
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
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    public Lesson save(CreateLessonDTO lessonDTO) {
        Course course = courseRepository.findById(lessonDTO.getCourseId())
                .orElseThrow(EntityNotFoundException::new);
        return lessonRepository.save(Lesson.builder()
                .name(lessonDTO.getLessonName())
                .course(course)
                .build());
    }

    @Override
    public Lesson findById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public List<Lesson> findAllLessons() {
        return lessonRepository.findAll();
    }

    @Override
    public List<Lesson> findAllLessonsByCourse(Long courseId) {
        return lessonRepository.findAllByCourseId(courseId);
    }

    @Override
    public List<Lesson> generateAndAssignLessons(Long numberOfLessons, Course course) {
        if (numberOfLessons < 5) {
            throw new LessonException("Course should have at least 5 lessons");
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
            Lesson lesson = findById(id);
            lessonRepository.delete(lesson);
            log.info("Lesson deleted successfully");
        } catch (EntityDeletionException e) {
            log.error("Error deletion lesson: {}", id);
            throw new EntityDeletionException("Failed to delete lesson", e);
        }
    }

    //TODO: test it
    @Override
    public Lesson editLesson(UpdateLessonDTO lesson) {
        findById(lesson.getId());
        Course course = courseRepository.findById(lesson.getId()).orElseThrow();
        Lesson editedLesson = Lesson.builder()
                .id(lesson.getId())
                .name(lesson.getName())
                .course(course)
                .build();
        return lessonRepository.update(editedLesson);
    }

    @Override
    public Lesson findLessonByHomeworkId(Long homeworkId) {
        return lessonRepository.findLessonByHomeworkId(homeworkId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
