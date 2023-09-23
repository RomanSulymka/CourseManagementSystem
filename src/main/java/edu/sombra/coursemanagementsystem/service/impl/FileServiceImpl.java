package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.repository.FileRepository;
import edu.sombra.coursemanagementsystem.service.FileService;
import edu.sombra.coursemanagementsystem.service.HomeworkService;
import edu.sombra.coursemanagementsystem.service.LessonService;
import edu.sombra.coursemanagementsystem.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
@Service
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final HomeworkService homeworkService;
    private final UserService userService;
    private final LessonService lessonService;

    @Transactional
    @Override
    public void saveFile(MultipartFile uploadedFile, Long lessonId, Long userId) throws IOException {
        User user = userService.findUserById(userId);
        Lesson lesson = lessonService.findById(lessonId);

        File file = fileRepository.save(File.builder()
                .fileName(uploadedFile.getOriginalFilename())
                .fileData(uploadedFile.getBytes())
                .build());
        log.info("File uploaded successfully with name {}", uploadedFile.getOriginalFilename());

        homeworkService.save(Homework.builder()
                .file(file)
                .user(user)
                .lesson(lesson)
                .build());
    }

    private File findFileByName(String fileName) {
        return fileRepository.findFileByName(fileName);
    }

    @Override
    public File getFileDataById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public String getFileNameById(Long fileId) {
        return fileRepository.findFileNameById(fileId);
    }

    @Override
    public Resource downloadFile(Long fileId) {
        try {
            File file = getFileDataById(fileId);

            if (file.getFileName() != null && file.getFileData() != null) {
                return new ByteArrayResource(file.getFileData()) {
                    @Override
                    public String getFilename() {
                        return file.getFileName();
                    }
                };
            } else {
                log.error("File with ID {} not found.", fileId);
                throw new NoResultException("File not found.");
            }
        } catch (DataAccessException e) {
            throw new NoResultException("File not found.");
        }
    }
}
