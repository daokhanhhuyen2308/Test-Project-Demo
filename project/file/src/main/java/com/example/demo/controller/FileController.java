package com.example.demo.controller;

import com.example.demo.dto.requests.FileDownloadDTO;
import com.example.demo.dto.responses.FileResponse;
import com.example.demo.exception.CustomExceptionHandler;
import com.example.demo.service.FileService;
import jakarta.validation.constraints.Email;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file,
                                        @Email(message = "Invalid recipient email") @RequestParam String recipient){

        if (file.isEmpty()){
            throw CustomExceptionHandler.badRequestException("File is not empty. Please choose any files!");
        }

        try{
            FileResponse fileResponse = fileService.uploadFile(file, recipient);
            return ResponseEntity.ok(fileResponse);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Loi upload file: " + e.getMessage());
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
