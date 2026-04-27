package com.pickkasso.pickkasso.global.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class S3ServiceTest {

    @Autowired
    private S3Service s3Service;

    private MultipartFile logoPng;
    private MultipartFile errorLogoWebp;
    private String uploadedUrl;

    @BeforeEach
    void setUp() throws IOException {
        logoPng = loadFile("static/images/logo.png", "image/png");
        errorLogoWebp = loadFile("static/images/error_logo.webp", "image/webp");
    }

    @AfterEach
    void tearDown() {
        if (uploadedUrl != null) {
            s3Service.delete(uploadedUrl);
            uploadedUrl = null;
        }
    }

    private MultipartFile loadFile(String path, String contentType) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        byte[] bytes = Files.readAllBytes(resource.getFile().toPath());
        String fileName = resource.getFilename();
        return new MockMultipartFile("file", fileName, contentType, bytes);
    }

    @Test
    @DisplayName("logo.png S3 업로드 성공")
    void upload_logoPng_success() throws IOException {
        uploadedUrl = s3Service.upload(logoPng, "test", "logo.png");

        System.out.println("logo.png URL: " + uploadedUrl);
        assertThat(uploadedUrl).contains("logo.png");
    }
}