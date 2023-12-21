package edu.sombra.coursemanagementsystem.service.impl;

import edu.sombra.coursemanagementsystem.dto.file.FileResponseDTO;
import edu.sombra.coursemanagementsystem.entity.Course;
import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.mapper.FileMapper;
import edu.sombra.coursemanagementsystem.repository.CourseRepository;
import edu.sombra.coursemanagementsystem.repository.FileRepository;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.FileService;
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
    public static final String FILE_DELETED_SUCCESSFULLY_BY_ADMIN = "file deleted successfully by admin";
    public static final String FILE_DELETED_SUCCESSFULLY = "file deleted successfully";
    public static final String USER_HAS_NO_PERMISSION_TO_DELETE_THIS_HOMEWORK = "User has no permission to delete this homework";
    public static final String FILE_NOT_FOUND = "File not found.";
    public static final String FILE_WITH_ID_NOT_FOUND = "File with ID {} not found.";
    public static final String INVALID_INPUT_PARAMETERS = "Invalid input parameters";
    public static final String FILE_UPLOADED_SUCCESSFULLY_WITH_NAME = "File uploaded successfully with name {}";

    private final FileRepository fileRepository;
    private final HomeworkRepository homeworkRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final FileMapper fileMapper;

    @Override
    public FileResponseDTO saveFile(MultipartFile uploadedFile, Long lessonId, Long userId) throws IOException {
        try {
            validateInput(uploadedFile, lessonId, userId);
            Homework existingHomework = findExistingHomework(lessonId, userId);

            File file = fileRepository.save(File.builder()
                    .fileName(uploadedFile.getOriginalFilename())
                    .fileData(uploadedFile.getBytes())
                    .build());
            log.info(FILE_UPLOADED_SUCCESSFULLY_WITH_NAME, uploadedFile.getOriginalFilename());

            updateHomeworkWithFile(existingHomework, file);
            return fileMapper.mapToResponseDTO(file);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private void updateHomeworkWithFile(Homework homework, File file) {
        homework.setFile(file);
        homeworkRepository.update(homework);
    }

    private Homework findExistingHomework(Long lessonId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id not found: " + userId);
        } else if (!lessonRepository.existsById(lessonId)) {
            throw new EntityNotFoundException("Lesson with id not found: " + lessonId);
        }
        return homeworkRepository.findByUserAndLessonId(userId, lessonId)
                .orElseThrow(EntityNotFoundException::new);
    }

    private static void validateInput(MultipartFile uploadedFile, Long lessonId, Long userId) {
        if (uploadedFile == null || lessonId == null || userId == null || lessonId <= 0 || userId <= 0) {
            throw new IllegalArgumentException(INVALID_INPUT_PARAMETERS);
        }
    }

    @Override
    public File getFileDataById(Long fileId) {
        return findFileById(fileId);
    }

    @Override
    public Resource downloadFile(Long fileId, String userEmail) {
        File file = getFileDataById(fileId);
        User user = userRepository.findUserByEmail(userEmail);

        if (canUserAccessFile(user, fileId)) {
            if (file.getFileName() != null && file.getFileData() != null) {
                return createFileResource(file);
            } else {
                log.error(FILE_WITH_ID_NOT_FOUND, fileId);
                throw new NoResultException(FILE_NOT_FOUND);
            }
        } else {
            throw new IllegalArgumentException("User hasn't access to this file!");
        }
    }

    private boolean canUserAccessFile(User user, Long fileId) {
        if (user.getRole().equals(RoleEnum.ADMIN)) {
            return true;
        } else {
            Course course = courseRepository.findCourseByFileId(fileId).orElseThrow();
            return courseRepository.isUserAssignedToCourse(user.getId(), course.getId());
        }
    }

    private ByteArrayResource createFileResource(File file) {
        return new ByteArrayResource(file.getFileData()) {
            @Override
            public String getFilename() {
                return file.getFileName();
            }
        };
    }

    @Override
    public void delete(Long fileId, String userEmail) {
        try {
            User user = userRepository.findUserByEmail(userEmail);
            File file = findFileById(fileId);
            if (user.getRole().equals(RoleEnum.ADMIN)) {
                fileRepository.delete(file);
                log.info(FILE_DELETED_SUCCESSFULLY_BY_ADMIN);
            } else {
                if (homeworkRepository.isUserUploadedHomework(fileId, user.getId())) {
                    fileRepository.delete(file);
                    log.info(FILE_DELETED_SUCCESSFULLY);
                } else {
                    throw new IllegalArgumentException(USER_HAS_NO_PERMISSION_TO_DELETE_THIS_HOMEWORK);
                }
            }
        } catch (Exception e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    private File findFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException(FILE_NOT_FOUND));
    }
}
