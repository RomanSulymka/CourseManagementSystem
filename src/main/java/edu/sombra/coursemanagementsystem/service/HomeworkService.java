package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;

import java.util.List;

public interface HomeworkService {

    void save(Homework homework);

    void setMark(Long userId, Long homeworkId, Long mark);

    boolean isUserUploadedThisHomework(Long fileId, Long studentId);

    GetHomeworkDTO findHomeworkById(Long homeworkId);

    String deleteHomework(Long homeworkId);

    List<GetHomeworkDTO> getAllHomeworks();

    List<GetHomeworkDTO> getAllHomeworksByUser(Long userId);
}