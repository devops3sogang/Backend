package com.devops3sogang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // 모든 필드를 받는 생성자를 만듭니다.
public class ErrorResponse {
    private int statusCode;
    private String message;
}