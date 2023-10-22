package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.exception.UserNotAssignedToCourseException;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.service.CourseMarkService;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import edu.sombra.coursemanagementsystem.service.HomeworkService;
import edu.sombra.coursemanagementsystem.service.LessonService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@AllArgsConstructor
@Slf4j
@Service
public class HomeworkServiceImpl implements HomeworkService {
    private final HomeworkRepository homeworkRepository;
    private final CourseMarkService courseMarkService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;

    @Override
    public void save(Homework homework) {
        homeworkRepository.save(homework);
        log.info("Homework saved successfully");
    }

    @Override
    public void setMark(Long userId, Long homeworkId, Long mark) {
        try {
            if (enrollmentService.isUserAssignedToCourse(userId, homeworkId)) {
                if (mark >= 0 && mark <= 100) {
                    homeworkRepository.setMark(homeworkId, mark);
                    log.info("Mark saved successfully");
                    Lesson lesson = lessonService.findLessonByHomeworkId(homeworkId);
                    Double averageMark = homeworkRepository.calculateAverageHomeworksMarkByUserId(userId, lesson.getCourse().getId());
                    courseMarkService.saveTotalMark(userId, lesson.getCourse().getId(), averageMark);
                } else {
                    log.error("Invalid mark value. Mark should be between 0 and 100. But now mark is {}", mark);
                    throw new IllegalArgumentException("Invalid mark value. Mark should be between 0 and 100.");
                }
            } else {
                throw new UserNotAssignedToCourseException("User isn't assigned to this course");
            }
        } catch (DataAccessException e) {
            throw new IllegalArgumentException("Invalid mark value. Mark should be between 0 and 100.");
        }
    }

    @Override
    public Homework findByUserAndLessonId(Long userId, Long lessonId) {
        return homeworkRepository.findByUserAndLessonId(userId, lessonId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
