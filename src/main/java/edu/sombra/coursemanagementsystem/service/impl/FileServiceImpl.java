package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.homework.GetHomeworkDTO;
import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.Lesson;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.FileRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.FileService;
import edu.sombra.coursemanagementsystem.service.HomeworkService;
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
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class FileServiceImpl implements FileService {
    public static final String FILE_DELETED_SUCCESSFULLY_BY_ADMIN = "file deleted successfully by admin";
    public static final String FILE_DELETED_SUCCESSFULLY = "file deleted successfully";
    public static final String USER_HAS_NO_PERMISSION_TO_DELETE_THIS_HOMEWORK = "User has no permission to delete this homework";
    public static final String FILE_NOT_FOUND = "File not found.";
    public static final String FILE_WITH_ID_NOT_FOUND = "File with ID {} not found.";
    public static final String USER_HAS_ALREADY_UPLOADED_THIS_HOMEWORK = "User has already uploaded this homework";
    public static final String INVALID_INPUT_PARAMETERS = "Invalid input parameters";
    public static final String FILE_UPLOADED_SUCCESSFULLY_WITH_NAME = "File uploaded successfully with name {}";

    private final FileRepository fileRepository;
    private final HomeworkService homeworkService;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    @Override
    public void saveFile(MultipartFile uploadedFile, Long lessonId, Long userId) throws IOException {
        try {
            validateInput(uploadedFile, lessonId, userId);
            if (!isUserAlreadyUploaded(userId, lessonId)) {
                User user = userRepository.findById(userId).orElseThrow();
                Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();

                File file = fileRepository.save(File.builder()
                        .fileName(uploadedFile.getOriginalFilename())
                        .fileData(uploadedFile.getBytes())
                        .build());
                log.info(FILE_UPLOADED_SUCCESSFULLY_WITH_NAME, uploadedFile.getOriginalFilename());

                homeworkService.save(Homework.builder()
                        .file(file)
                        .user(user)
                        .lesson(lesson)
                        .build());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private static void validateInput(MultipartFile uploadedFile, Long lessonId, Long userId) {
        if (uploadedFile == null || lessonId == null || userId == null || lessonId <= 0 || userId <= 0) {
            throw new IllegalArgumentException(INVALID_INPUT_PARAMETERS);
        }
    }

    private boolean isUserAlreadyUploaded(Long userId, Long lessonId) {
        List<GetHomeworkDTO> homeworks = homeworkService.getAllHomeworksByUser(userId);
        for (GetHomeworkDTO homework : homeworks) {
            if (!homework.getLesson().getId().equals(lessonId)) {
                throw new IllegalArgumentException(USER_HAS_ALREADY_UPLOADED_THIS_HOMEWORK);
            }
        }
        return false;
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
            log.error(FILE_WITH_ID_NOT_FOUND, fileId);
            throw new NoResultException(FILE_NOT_FOUND);
        }
    }

    @Override
    public void delete(Long fileId, String userEmail) {
        User user = userRepository.findUserByEmail(userEmail);
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            File file = findFileById(fileId);
            fileRepository.delete(file);
            log.info(FILE_DELETED_SUCCESSFULLY_BY_ADMIN);
        } else {
            if (homeworkService.isUserUploadedThisHomework(fileId, user.getId())) {
                File file = findFileById(fileId);
                fileRepository.delete(file);
                log.info(FILE_DELETED_SUCCESSFULLY);
            } else {
                throw new IllegalArgumentException(USER_HAS_NO_PERMISSION_TO_DELETE_THIS_HOMEWORK);
            }
        }
    }

    private File findFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(EntityNotFoundException::new);
    }
}
