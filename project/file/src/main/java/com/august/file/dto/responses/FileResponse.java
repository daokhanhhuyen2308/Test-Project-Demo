package com.august.file.dto.responses;

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

}
