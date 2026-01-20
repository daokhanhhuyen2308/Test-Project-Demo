package com.example.demo.mapper;

import com.example.demo.dto.responses.FileResponse;
import com.example.demo.entity.FileEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileResponse mapToResponse(FileEntity entity);




}
