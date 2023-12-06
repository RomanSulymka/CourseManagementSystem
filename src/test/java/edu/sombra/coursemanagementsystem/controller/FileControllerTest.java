package edu.sombra.coursemanagementsystem.controller;

import edu.sombra.coursemanagementsystem.entity.File;
import edu.sombra.coursemanagementsystem.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.FileCopyUtils;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello, World!".getBytes());

        mockMvc.perform(multipart("/api/v1/files/upload/{userId}/{lessonId}", 2, 5)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));

        verify(fileService, times(1)).saveFile(file, 5L, 2L);
    }

    @Test
    void testUploadFileUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello, World!".getBytes());

        mockMvc.perform(multipart("/api/v1/files/upload/{userId}/{lessonId}", 1, 2)
                        .file(file)
                        .param("userId", "1")
                        .param("lessonId", "2"))
                .andExpect(status().isUnauthorized());

        verify(fileService, never()).saveFile(file, 2L, 1L);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDownloadFile() throws Exception {
        Long fileId = 2L;
        String fileName = "file2.txt";
        File file = File.builder()
                .id(2L)
                .fileData("gegegegege".getBytes())
                .fileName(fileName)
                .build();
        Resource mockFileResource = new ByteArrayResource(file.getFileData()) {
            @Override
            public String getFilename() {
                return file.getFileName();
            }
        };
        when(fileService.downloadFile(fileId)).thenReturn(mockFileResource);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/files/download/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().bytes(FileCopyUtils.copyToByteArray(mockFileResource.getInputStream())));

        verify(fileService, times(1)).downloadFile(fileId);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDownloadFileNotFound() throws Exception {
        Long fileId = 2L;

        when(fileService.downloadFile(fileId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/files/download/{fileId}", fileId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void testDeleteFile() throws Exception {
        Long fileId = 1L;

        doNothing().when(fileService).delete(fileId, "admin@gmail.com");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/files/{fileId}", fileId)
                        .with(request -> {
                            request.setRemoteUser("admin@gmail.com");
                            return request;
                        }))
                .andExpect(status().isOk());

        verify(fileService).delete(fileId, "admin@gmail.com");
    }
}
