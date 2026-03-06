package com.august.file.mapper;


public final class FilePurposeConverter {
    private FilePurposeConverter() {}
    public static com.august.file.enums.FilePurposeEntity toEntity(
            com.august.protocol.file.FilePurpose purpose
    ) {
        return com.august.file.enums.FilePurposeEntity.valueOf(purpose.name());
    }
}
