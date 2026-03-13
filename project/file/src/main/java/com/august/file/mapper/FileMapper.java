package com.august.file.mapper;

import com.august.file.dto.FileResponse;
import com.august.file.entity.FileEntity;
import com.august.protocol.file.UploadFileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMapper {
    FileResponse mapToResponse(FileEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "filePath", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "size", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "viewable", ignore = true)
    @Mapping(target = "purpose", ignore = true)
    FileEntity mapToFileEntity(UploadFileRequest request);
}
