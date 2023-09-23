package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.dto.homework.HomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.Homework;

public interface HomeworkService {

    void save(Homework homework);

    void setMark(Long homeworkId, Long mark);
}
