package com.devops3sogang.backend.dto.crawler;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuData {
    @JsonProperty("menuList")
    private List<DailyMenuData> menuList;
}