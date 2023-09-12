package edu.sombra.coursemanagementsystem.repository;

import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.repository.base.BaseRepository;

import java.util.Optional;

public interface CourseRepository extends BaseRepository<Course, Long> {
    Optional<Course> findByName(String name);

    boolean exist(String name);
}
