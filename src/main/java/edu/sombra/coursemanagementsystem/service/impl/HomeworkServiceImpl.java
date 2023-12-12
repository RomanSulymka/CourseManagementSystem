package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.exception.UserNotAssignedToCourseException;
import edu.sombra.coursemanagementsystem.mapper.HomeworkMapper;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.CourseMarkService;
import edu.sombra.coursemanagementsystem.service.EnrollmentService;
import edu.sombra.coursemanagementsystem.service.HomeworkService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@AllArgsConstructor
@Slf4j
@Service
public class HomeworkServiceImpl implements HomeworkService {
    public static final String HOMEWORK_DELETED_SUCCESSFULLY = "Homework deleted successfully!";
    public static final String HOMEWORK_SAVED_SUCCESSFULLY = "Homework saved successfully";
    public static final String HOMEWORK_CANNOT_BE_NULL = "Homework cannot be null";
    public static final String MARK_SAVED_SUCCESSFULLY = "Mark saved successfully";
    public static final String INVALID_MARK_VALUE_MARK_SHOULD_BE_BETWEEN_0_AND_100_BUT_NOW_MARK_IS = "Invalid mark value. Mark should be between 0 and 100. But now mark is {}";
    public static final String INVALID_MARK_VALUE_MARK_SHOULD_BE_BETWEEN_0_AND_100 = "Invalid mark value. Mark should be between 0 and 100.";
    public static final String USER_ISN_T_ASSIGNED_TO_THIS_COURSE = "User isn't assigned to this course";

    private final HomeworkRepository homeworkRepository;
    private final CourseMarkService courseMarkService;
    private final LessonRepository lessonRepository;
    private final EnrollmentService enrollmentService;
    private final HomeworkMapper homeworkMapper;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    public void save(Homework homework) {
        if (homework == null) {
            throw new IllegalArgumentException(HOMEWORK_CANNOT_BE_NULL);
        }
        homeworkRepository.save(homework);
        log.info(HOMEWORK_SAVED_SUCCESSFULLY);
    }

    @Override
    public GetHomeworkDTO setMark(Long userId, Long homeworkId, Long mark) {
        if (enrollmentService.isUserAssignedToCourse(userId, homeworkId)) {
            if (mark >= 0 && mark <= 100) {
                homeworkRepository.setMark(homeworkId, mark);
                log.info(MARK_SAVED_SUCCESSFULLY);
                Lesson lesson = lessonRepository.findLessonByHomeworkId(homeworkId)
                        .orElseThrow(EntityNotFoundException::new);

                Double averageMark = homeworkRepository.calculateAverageHomeworksMarkByUserId(userId, lesson.getCourse().getId());
                boolean isAllHomeworksGraded = isAllHomeworksGraded(userId, lesson.getCourse().getId());
                courseMarkService.saveTotalMark(userId, lesson.getCourse().getId(), averageMark, isAllHomeworksGraded);
                return findAndMapToDTO(homeworkId);
            } else {
                log.error(INVALID_MARK_VALUE_MARK_SHOULD_BE_BETWEEN_0_AND_100_BUT_NOW_MARK_IS, mark);
                throw new IllegalArgumentException(INVALID_MARK_VALUE_MARK_SHOULD_BE_BETWEEN_0_AND_100);
            }
        } else {
            throw new UserNotAssignedToCourseException(USER_ISN_T_ASSIGNED_TO_THIS_COURSE);
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
    public GetHomeworkDTO findHomeworkById(Long homeworkId, String userEmail) {
        User user = userRepository.findUserByEmail(userEmail);
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            return findAndMapToDTO(homeworkId);
        } else {
            Course course = courseRepository.findCourseByHomeworkId(homeworkId).orElseThrow(EntityNotFoundException::new);
            boolean isUserAssignedToCourse = courseRepository.isUserAssignedToCourse(user.getId(), course.getId());
            if (isUserAssignedToCourse) {
                return findAndMapToDTO(homeworkId);
            } else {
                throw new IllegalArgumentException("User hasn't access to this homework!");
            }
        }
    }

    private GetHomeworkDTO findAndMapToDTO(Long homeworkId) {
        Homework homework = findHomework(homeworkId);
        return homeworkMapper.mapToDTO(homework);
    }

    @Override
    public String deleteHomework(Long homeworkId) {
        Homework homework = findHomework(homeworkId);
        homeworkRepository.delete(homework);
        Double averageMark = homeworkRepository.calculateAverageHomeworksMarkByUserId(homework.getUser().getId(), homework.getLesson().getCourse().getId());
        courseMarkService.saveTotalMark(homework.getUser().getId(), homework.getLesson().getCourse().getId(), averageMark, false);
        log.info(HOMEWORK_DELETED_SUCCESSFULLY);
        return HOMEWORK_DELETED_SUCCESSFULLY;
    }

    @Override
    public List<GetHomeworkDTO> getAllHomeworks() {
        //TODO: student and instructor can see just his homeworks || homeworks on the course
        List<Homework> homeworkList = homeworkRepository.findAll();
        return homeworkMapper.mapToDTO(homeworkList);
    }

    @Override
    public List<GetHomeworkDTO> getAllHomeworksByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        List<Homework> homeworkList = homeworkRepository.findAllByUser(user.getId());
        return homeworkMapper.mapToDTO(homeworkList);
    }

    @Override
    public GetHomeworkDTO findHomeworkByUserAndLessonId(Long userId, Long lessonId) {
        userRepository.findById(userId);
        var lesson = lessonRepository.findById(lessonId)
                .orElseThrow(EntityNotFoundException::new);

        Homework homework = homeworkRepository.findByUserAndLessonId(userId, lesson.getId())
                .orElseThrow(EntityNotFoundException::new);
        return homeworkMapper.mapToDTO(homework);
    }

    public Homework findHomework(Long homeworkId) {
        return homeworkRepository.findById(homeworkId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
