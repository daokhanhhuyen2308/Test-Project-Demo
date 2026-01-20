package com.example.demo.service;

import com.example.demo.dto.requests.FileDownloadDTO;
import com.example.demo.dto.responses.FileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileResponse uploadFile(MultipartFile file, String recipient);

    FileDownloadDTO extractFile(String fileId);
}
