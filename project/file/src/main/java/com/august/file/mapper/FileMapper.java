package com.august.file.mapper;

import com.august.file.dto.responses.FileResponse;
import com.august.file.entity.FileEntity;
import com.august.protocol.profile.UploadFileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {
    FileResponse mapToResponse(FileEntity entity);
}
