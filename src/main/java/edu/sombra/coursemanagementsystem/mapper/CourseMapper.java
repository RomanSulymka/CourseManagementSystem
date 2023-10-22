package edu.sombra.coursemanagementsystem.mapper;

import edu.sombra.coursemanagementsystem.dto.course.LessonsByCourseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.CourseFeedback;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.repository.CourseFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseMapper {
    private final LessonMapper lessonMapper;
    private final CourseFeedbackRepository courseFeedbackRepository;

    public LessonsByCourseDTO toDTO(Course course, List<Lesson> lessons, BigDecimal totalScore, Long studentId) {
        CourseFeedback feedback = courseFeedbackRepository.findFeedback(studentId, course.getId())
                .orElse(null);
        if (feedback == null) {
            return LessonsByCourseDTO.builder()
                    .courseId(course.getId())
                    .courseName(course.getName())
                    .feedback(null)
                    .totalScore(totalScore)
                    .lessonDTO(lessonMapper.toDTO(lessons, studentId))
                    .build();
        } else {
            return LessonsByCourseDTO.builder()
                    .courseId(course.getId())
                    .courseName(course.getName())
                    .feedback(feedback.getFeedbackText())
                    .totalScore(totalScore)
                    .lessonDTO(lessonMapper.toDTO(lessons, studentId))
                    .build();
        }
    }
}
