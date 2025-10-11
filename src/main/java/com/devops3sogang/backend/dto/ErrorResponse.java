package com.devops3sogang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;           // HTTP 상태 코드 (404, 500 등)
    private String code;          // 에러 코드 (NOT_FOUND, INTERNAL_SERVER_ERROR 등)
    private String message;       // 에러 메시지
    
    // 기존 코드와 호환성을 위한 생성자
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.code = "ERROR";
    }
}