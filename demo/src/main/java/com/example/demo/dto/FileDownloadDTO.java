package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileDownloadDTO {
    private Resource resource;
    private String contentType;
    private String originalName;
    private boolean isViewable;
}
