package com.devops3sogang.backend.dto.crawler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DailyMenuData {
    private String menuDate;

    @JsonProperty("menuInfo")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<MenuInfo> menuInfo = new ArrayList<>();
}