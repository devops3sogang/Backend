package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Image Controller", description = "이미지 업로드/조회 API")
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지 파일 업로드
     * POST /images/upload
     */
    @PostMapping("/images/upload")
    @Operation(summary = "이미지 업로드", description = "이미지 파일을 업로드하고 URL을 반환합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageService.saveImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "이미지 업로드 성공");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("이미지 업로드 실패 - 유효하지 않은 파일: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "이미지 업로드 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 이미지 파일 서빙
     * GET /uploads/images/{filename}
     */
    @GetMapping("/uploads/images/{filename}")
    @Operation(summary = "이미지 조회", description = "저장된 이미지 파일을 반환합니다.")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Resource resource = imageService.loadImage(filename);

            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.info("이미지 조회 실패 - 파일 없음: {}", filename);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.info("이미지 조회 실패: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
