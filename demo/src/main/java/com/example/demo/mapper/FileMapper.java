package com.example.demo.mapper;

import com.example.demo.dto.FileResponse;
import com.example.demo.entity.FileEntity;

public class FileMapper {
    public static FileResponse mapToFileResponse(FileEntity entity){
        FileResponse response = new FileResponse();
        response.setId(entity.getId());
        response.setUploadedAt(entity.getUploadedAt());
        response.setFileName(entity.getFileName());
        response.setSize(entity.getSize());
        response.setContentType(entity.getContentType());
        return response;
    }
}
