package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.exception.LessonException;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import edu.sombra.coursemanagementsystem.service.CourseService;
import edu.sombra.coursemanagementsystem.service.LessonService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final CourseService courseService;

    @Override
    public Lesson save(CreateLessonDTO lessonDTO) {
        try {
            Course course = courseService.findById(lessonDTO.getCourseId());
            return lessonRepository.save(Lesson.builder()
                    .name(lessonDTO.getLessonName())
                    .course(course)
                    .build());
        } catch (DataAccessException ex) {
            log.error("Error creating lesson: {}", ex.getMessage(), ex);
            throw new LessonException("Failed to create lesson", ex);
        }
    }

    @Override
    public List<Lesson> findAllLessons() {
        return lessonRepository.findAll();
    }

    @Override
    public List<Lesson> findAllLessonsByCourse(Long courseId) {
        return lessonRepository.findAllByCourseId(courseId);
    }
}
