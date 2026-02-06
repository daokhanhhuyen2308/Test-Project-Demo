package com.august.file.mapper;

import com.august.file.dto.responses.FileResponse;
import com.august.file.entity.FileEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileResponse mapToResponse(FileEntity entity);




}
