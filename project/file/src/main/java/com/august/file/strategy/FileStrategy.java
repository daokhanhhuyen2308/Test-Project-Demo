package com.august.file.strategy;

import com.august.protocol.file.FilePurpose;
import com.august.protocol.file.UploadFileRequest;

import java.nio.file.Path;

public interface FileStrategy {
    FilePurpose getPurpose();
    void validate(UploadFileRequest request);
    Path resolveDirectory(Path rootDirectory, UploadFileRequest request);
}
