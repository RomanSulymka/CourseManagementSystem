package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

public interface HomeworkRepository extends BaseRepository<Homework, Long> {
    void setMark(Long homeworkId, Long mark);
}
