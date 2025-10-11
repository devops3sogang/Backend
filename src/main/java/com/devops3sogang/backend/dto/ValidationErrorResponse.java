package com.devops3sogang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    private int status;                      // HTTP 상태 코드
    private String code;                     // 에러 코드 ("VALIDATION_ERROR")
    private String message;                  // 기본 메시지
    private Map<String, String> errors;      // 필드별 에러 메시지
}