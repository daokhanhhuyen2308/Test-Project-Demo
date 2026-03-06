package com.august.file.service;

import com.august.file.dto.requests.FileDownloadDTO;
import com.august.file.dto.responses.FileResponse;
import com.august.protocol.file.UploadFileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileResponse uploadFile(MultipartFile file);

    FileDownloadDTO extractFile(String fileId);
    FileResponse uploadFileFromGrpc(UploadFileRequest fileRequest);
}
