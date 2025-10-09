package com.devops3sogang.backend.dto.crawler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuInfo {
    private String category;
    private String menu;
}