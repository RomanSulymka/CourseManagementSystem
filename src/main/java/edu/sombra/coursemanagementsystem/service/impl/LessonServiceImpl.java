package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.course.CourseResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.CreateLessonDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.LessonResponseDTO;
import edu.sombra.coursemanagementsystem.dto.lesson.UpdateLessonDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.exception.EntityDeletionException;
import edu.sombra.coursemanagementsystem.exception.LessonException;
import edu.sombra.coursemanagementsystem.mapper.CourseMapper;
import edu.sombra.coursemanagementsystem.mapper.LessonMapper;
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
    public LessonResponseDTO findById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        CourseResponseDTO courseResponse = courseMapper.mapToResponseDTO(lesson.getCourse());
        return lessonMapper.mapToResponseDTO(lesson, courseResponse);
    }

    @Override
    public List<LessonResponseDTO> findAllLessons() {
        List<Lesson> lessons = lessonRepository.findAll();
        List<CourseResponseDTO> courseResponseDTOS = lessons.stream()
                .map(lesson -> courseMapper.mapToResponseDTO(lesson.getCourse()))
                .toList();

        return lessonMapper.mapToResponsesDTO(lessons, courseResponseDTOS);
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
            Lesson lesson = lessonRepository.findById(id)
                    .orElseThrow(EntityNotFoundException::new);
            lessonRepository.delete(lesson);
            log.info("Lesson deleted successfully");
        } catch (EntityDeletionException e) {
            log.error("Error deletion lesson: {}", id);
            throw new EntityDeletionException("Failed to delete lesson", e);
        }
    }

    //TODO: test it
    @Override
    public LessonResponseDTO editLesson(UpdateLessonDTO lesson) {
        findById(lesson.getId());
        Course course = courseRepository.findById(lesson.getId()).orElseThrow();
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
