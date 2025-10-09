package com.devops3sogang.backend.dto.crawler;

import lombok.Data;

@Data
public class CrawledMenuResponse {
    private int statusCode;
    private String responseMessage;
    private MenuData data;
}