package com.august.file.dto.responses;

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
    private long size;
    private Instant uploadedAt;
    private String contentType;
    private FilePurposeEntity purpose;

}
