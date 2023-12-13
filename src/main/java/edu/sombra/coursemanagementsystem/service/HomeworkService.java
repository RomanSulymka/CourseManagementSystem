package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;

import java.util.List;

public interface HomeworkService {

    void save(Homework homework);

    GetHomeworkDTO setMark(Long userId, Long homeworkId, Long mark);

    boolean isUserUploadedThisHomework(Long fileId, Long studentId);

    GetHomeworkDTO findHomeworkById(Long homeworkId, String userEmail);

    String deleteHomework(Long homeworkId);

    List<GetHomeworkDTO> getAllHomeworks(String userEmail);

    List<GetHomeworkDTO> getAllHomeworksByUser(Long userId);

    GetHomeworkDTO findHomeworkByUserAndLessonId(Long userId, Long lessonId);
}
