package com.devops3sogang.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 이미지 파일 저장
     * @param file 업로드된 파일
     * @return 저장된 파일의 URL
     */
    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif만 가능)");
        }

        // 파일 크기 검증 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 고유한 파일명 생성
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // URL 반환
        String imageUrl = "/uploads/images/" + filename;
        log.info("이미지 저장 완료: {}", imageUrl);

        return imageUrl;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }

    /**
     * 이미지 확장자 검증
     */
    private boolean isValidImageExtension(String extension) {
        return extension.equals(".jpg") ||
               extension.equals(".jpeg") ||
               extension.equals(".png") ||
               extension.equals(".gif");
    }

    /**
     * 이미지 파일 로드
     * @param filename 파일명
     * @return Resource
     */
    public Resource loadImage(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            log.info("이미지 조회 시도 - uploadDir: {}, filename: {}, fullPath: {}", uploadDir, filename, filePath.toAbsolutePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("이미지 로드 성공: {}", filename);
                return resource;
            } else {
                log.info("이미지 파일을 찾을 수 없음 - fullPath: {}, exists: {}, readable: {}",
                    filePath.toAbsolutePath(), resource.exists(), resource.isReadable());
                throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException e) {
            log.info("이미지 로드 실패 - 잘못된 경로: {}", filename, e);
            throw new IllegalArgumentException("잘못된 파일 경로입니다: " + filename);
        }
    }
}
