package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.exception.UserNotAssignedToCourseException;
import edu.sombra.coursemanagementsystem.mapper.HomeworkMapper;
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

import java.util.List;

@Transactional
@AllArgsConstructor
@Slf4j
@Service
public class HomeworkServiceImpl implements HomeworkService {
    private final HomeworkRepository homeworkRepository;
    private final CourseMarkService courseMarkService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final HomeworkMapper homeworkMapper;

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
                    Boolean isAllHomeworksGraded = isAllHomeworksGraded(userId, lesson.getCourse().getId());
                    courseMarkService.saveTotalMark(userId, lesson.getCourse().getId(), averageMark, isAllHomeworksGraded);
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

    private boolean isAllHomeworksGraded(Long userId, Long courseId) {
        List<Homework> homeworks = homeworkRepository.findHomeworksByCourse(courseId);
        for (Homework homework : homeworks) {
            if (homework.getUser().getId().equals(userId) && (homework.getMark() == null)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isUserUploadedThisHomework(Long fileId, Long studentId) {
        return homeworkRepository.isUserUploadedHomework(fileId, studentId);
    }

    @Override
    public GetHomeworkDTO findHomeworkById(Long homeworkId) {
        Homework homework = findHomework(homeworkId);
        return homeworkMapper.mapToDTO(homework);
    }

    @Override
    public String deleteHomework(Long homeworkId) {
        Homework homework = findHomework(homeworkId);
        homeworkRepository.delete(homework);
        log.info("Homework deleted successfully!");
        return "Homework deleted successfully!";
    }

    @Override
    public List<GetHomeworkDTO> getAllHomeworks() {
        List<Homework> homeworkList = homeworkRepository.findAll();
        return homeworkMapper.mapToDTO(homeworkList);
    }

    @Override
    public List<GetHomeworkDTO> getAllHomeworksByUser(Long userId) {
        List<Homework> homeworkList = homeworkRepository.findAllByUser(userId);
        return homeworkMapper.mapToDTO(homeworkList);
    }

    private Homework findHomework(Long homeworkId) {
        return homeworkRepository.findById(homeworkId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
