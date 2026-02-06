package com.august.file.service;

import com.august.file.dto.requests.FileDownloadDTO;
import com.august.file.dto.responses.FileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileResponse uploadFile(MultipartFile file, String recipient);

    FileDownloadDTO extractFile(String fileId);
}
