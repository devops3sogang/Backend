package com.devops3sogang.backend.controller;

import com.devops3sogang.backend.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Image Controller", description = "이미지 업로드 API")
public class ImageController {

    private final ImageService imageService;

    /**
     * 이미지 파일 업로드
     * POST /images/upload
     */
    @PostMapping("/upload")
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
}
