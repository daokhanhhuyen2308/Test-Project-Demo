package com.august.file.dto;

import com.august.file.enums.FilePurposeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private String id;
    private String fileName;
    private String filePath;
    private String ownerId;
    private long size;
    private Instant uploadedAt;
    private boolean isViewable;
    private String contentType;
    private FilePurposeEntity purpose;

}
