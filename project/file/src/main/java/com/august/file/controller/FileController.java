package com.august.file.controller;

import com.august.file.dto.requests.FileDownloadDTO;
import com.august.file.dto.responses.FileResponse;
import com.august.file.service.FileService;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file, @AuthenticationPrincipal Jwt jwt){

        String recipient = jwt.getClaimAsString("email");

        if (file.isEmpty()){
            throw new AppCustomException(ErrorCode.UPLOAD_FAILED);
        }

        try{
            FileResponse fileResponse = fileService.uploadFile(file, recipient);
            return ResponseEntity.ok(fileResponse);
        } catch (Exception e) {
            throw new AppCustomException(ErrorCode.UPLOAD_FILE_ERROR);
        }
    }


    @GetMapping("/export/{fileId}")
    public ResponseEntity<Resource> extractFile(@PathVariable String fileId){
        FileDownloadDTO fileDownloadDTO = fileService.extractFile(fileId);

        String disposition = fileDownloadDTO.isViewable() ? "inline" : "attachment";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDownloadDTO.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename=\"" + fileDownloadDTO.getOriginalName() + "\""
                )
                .body(fileDownloadDTO.getResource());
    }

}
