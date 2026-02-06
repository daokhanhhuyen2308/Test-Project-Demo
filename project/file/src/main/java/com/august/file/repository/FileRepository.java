package com.august.file.repository;

import com.august.file.entity.FileEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends MongoRepository<FileEntity, String> {
    FileEntity findFileEntityById(String fileId);
}
