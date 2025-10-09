package com.devops3sogang.backend.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "onCampusMenus")
public class OnCampusMenu {
    @Id
    private String id;
    private String restaurantId;
    private String restaurantName;
    private String weekStartDate;
    private List<DailyMenu> dailyMenus;
}