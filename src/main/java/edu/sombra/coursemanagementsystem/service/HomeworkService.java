package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.entity.Homework;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface HomeworkService {

    void save(Homework homework);
}
