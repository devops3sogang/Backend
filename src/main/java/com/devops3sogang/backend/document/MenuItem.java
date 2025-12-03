package com.devops3sogang.backend.document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class MenuItem {
    @ID
    private String id;
    private String name;
    private int price;
}