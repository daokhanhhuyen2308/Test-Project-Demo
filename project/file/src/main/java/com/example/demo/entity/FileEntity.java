package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Setter
@Getter
@Document(collection = "file")
public class FileEntity {
    @Id
    private String id;
    private String fileName;
    private String filePath;
    private String ownerId;
    private long size;
    private Instant uploadedAt;
    private String contentType;
    private boolean isViewable;

    public FileEntity() {
        this.uploadedAt = Instant.now();
    }

}
