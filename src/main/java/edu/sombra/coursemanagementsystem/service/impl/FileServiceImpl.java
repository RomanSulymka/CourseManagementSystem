package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final HomeworkService homeworkService;
    private final UserService userService;
    private final LessonService lessonService;

    @Override
    public void saveFile(MultipartFile uploadedFile, Long lessonId, Long userId) throws IOException {
        validateInput(uploadedFile, lessonId, userId);

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

    private static void validateInput(MultipartFile uploadedFile, Long lessonId, Long userId) {
        if (uploadedFile == null || lessonId == null || userId == null || lessonId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Invalid input parameters");
        }
    }

    @Override
    public File getFileDataById(Long fileId) {
        return findFileById(fileId);
    }

    @Override
    public Resource downloadFile(Long fileId) {
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
    }

    @Override
    public void delete(Long fileId, String userEmail) {
        User user = userService.findUserByEmail(userEmail);
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            File file = findFileById(fileId);
            fileRepository.delete(file);
            log.info("file deleted successfully by admin");
        } else {
            if (homeworkService.isUserUploadedThisHomework(fileId, user.getId())) {
                File file = findFileById(fileId);
                fileRepository.delete(file);
                log.info("file deleted successfully");
            } else {
                throw new IllegalArgumentException("User has no permission to delete this homework");
            }
        }
    }

    private File findFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
