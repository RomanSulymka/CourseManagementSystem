package edu.sombra.coursemanagementsystem.service;

import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.entity.Homework;
import edu.sombra.coursemanagementsystem.entity.User;
import edu.sombra.coursemanagementsystem.enums.RoleEnum;
import edu.sombra.coursemanagementsystem.repository.FileRepository;
import edu.sombra.coursemanagementsystem.repository.HomeworkRepository;
import edu.sombra.coursemanagementsystem.repository.LessonRepository;
import edu.sombra.coursemanagementsystem.repository.UserRepository;
import edu.sombra.coursemanagementsystem.service.impl.FileServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    private FileService fileService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private HomeworkRepository homeworkRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fileService = new FileServiceImpl(fileRepository, homeworkRepository, userRepository, lessonRepository);
    }

    public static Object[][] provideFileTest() {
        return new Object[][]{
                {1L, "text1.txt", "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."},
                {2L, "text2.txt", "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like)."},
                {3L, "text3.txt", "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable. If you are going to use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden in the middle of text. All the Lorem Ipsum generators on the Internet tend to repeat predefined chunks as necessary, making this the first true generator on the Internet. It uses a dictionary of over 200 Latin words, combined with a handful of model sentence structures, to generate Lorem Ipsum which looks reasonable. The generated Lorem Ipsum is therefore always free from repetition, injected humour, or non-characteristic words etc."}
        };
    }

    public static Object[][] testFiles() {
        return new Object[][]{
                {"testFile1.txt", new byte[]{1, 2, 3, 4, 5}, 1L, 11L},
                {"testFile2.txt", new byte[]{6, 7, 8, 9, 1}, 2L, 12L},
                {"testFile3.txt", new byte[]{5, 2, 1, 6, 9}, 2L, 13L},
        };
    }

    public static Object[][] testIncorrectInput() {
        return new Object[][]{
                {null, -1L, 0L},
                {"testFile.txt", 2L, -2L},
                {"1111.txt", -1L, 13L},
        };
    }

    @ParameterizedTest
    @MethodSource("testFiles")
    void testSaveFile_Successful(String fileName, byte[] fileData, Long userId, Long lessonId) throws IOException {
        MultipartFile uploadedFile = mock(MultipartFile.class);
        Homework existingHomework = mock(Homework.class);

        when(uploadedFile.getOriginalFilename()).thenReturn(fileName);
        when(uploadedFile.getBytes()).thenReturn(fileData);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(lessonRepository.existsById(lessonId)).thenReturn(true);
        when(homeworkRepository.findByUserAndLessonId(userId, lessonId)).thenReturn(Optional.ofNullable(existingHomework));

        File file = new File();
        when(fileRepository.save(any(File.class))).thenReturn(file);

        fileService.saveFile(uploadedFile, lessonId, userId);

        verify(fileRepository, times(1)).save(any(File.class));

        verify(homeworkRepository, times(1)).update(existingHomework);
    }

    @ParameterizedTest
    @MethodSource("testIncorrectInput")
    void testSaveFile_InvalidInput(String fileName, Long lessonId, Long userId) {
        MultipartFile uploadedFile = mock(MultipartFile.class);
        lenient().when(uploadedFile.getOriginalFilename()).thenReturn(fileName);
        assertThrows(IllegalArgumentException.class, () -> fileService.saveFile(uploadedFile, lessonId, userId));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L})
    void testGetFileDataByIdWithExistingFile(Long fileId) {
        File expectedFile = new File();

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(expectedFile));

        File result = fileService.getFileDataById(fileId);

        assertEquals(expectedFile, result);
    }

    @ParameterizedTest
    @ValueSource(longs = {4L, 5L, 6L})
    void testGetFileDataByIdWithNonExistingFile(Long fileId) {
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.getFileDataById(fileId));
    }

    @ParameterizedTest
    @ValueSource(longs = {4L, 5L, 6L})
    void testGetFileDataById_ExistingFile(Long fileId) {
        File expectedFile = new File();
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(expectedFile));

        File result = fileService.getFileDataById(fileId);

        assertEquals(expectedFile, result);
    }

    @ParameterizedTest
    @ValueSource(longs = {4L, 5L, 6L})
    void testGetFileDataById_NonExistingFile(Long fileId) {
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.getFileDataById(fileId));
    }

    @Test
    void testDelete_AdminUser() {
        Long fileId = 1L;
        String adminEmail = "admin@example.com";
        User adminUser = User.builder()
                .email(adminEmail)
                .role(RoleEnum.ADMIN)
                .build();
        when(userRepository.findUserByEmail(adminEmail)).thenReturn(adminUser);
        File file = new File();
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        fileService.delete(fileId, adminEmail);

        verify(fileRepository).delete(file);
    }

    @Test
    void testDelete_NormalUser_WithPermission() {
        Long fileId = 2L;
        String userEmail = "user@example.com";
        User normalUser = User.builder()
                .email(userEmail)
                .role(RoleEnum.STUDENT)
                .build();
        when(userRepository.findUserByEmail(userEmail)).thenReturn(normalUser);
        File file = new File();
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(homeworkRepository.isUserUploadedHomework(fileId, normalUser.getId())).thenReturn(true);

        fileService.delete(fileId, userEmail);

        verify(fileRepository).delete(file);
    }

    @Test
    void testDelete_NormalUser_NoPermission() {
        Long fileId = 3L;
        String userEmail = "user@example.com";
        User normalUser = User.builder()
                .email(userEmail)
                .role(RoleEnum.STUDENT)
                .build();
        when(userRepository.findUserByEmail(userEmail)).thenReturn(normalUser);
        //when(homeworkRepository.isUserUploadedThisHomework(fileId, normalUser.getId())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> fileService.delete(fileId, userEmail));
        verify(fileRepository, never()).delete(any());
    }

    @ParameterizedTest
    @MethodSource("provideFileTest")
    void testDownloadFile_SuccessfulDownload(Long fileId, String fileName, String fileData) {
        File file = new File();
        file.setFileName(fileName);
        file.setFileData(fileData.getBytes());
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        Resource resource = fileService.downloadFile(fileId);

        assertNotNull(resource);
        assertTrue(resource instanceof ByteArrayResource);
        assertEquals(fileName, resource.getFilename());
    }

    @Test
    void testDownloadFile_FileDataOrNameIsNull() {
        Long fileId = 2L;
        File file = new File();
        file.setFileName(null);
        file.setFileData(new byte[]{1, 2, 3});
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        assertThrows(NoResultException.class, () -> fileService.downloadFile(fileId));
    }

    @Test
    void testDownloadFile_FileNotFound() {
        Long fileId = 3L;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> fileService.downloadFile(fileId));
    }
}
