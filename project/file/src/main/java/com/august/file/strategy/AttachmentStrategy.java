package com.august.file.strategy;

import com.august.protocol.profile.FilePurpose;
import com.august.protocol.profile.UploadFileRequest;

import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class AttachmentStrategy implements FileStrategy{

    @Override
    public FilePurpose getPurpose() {
        return FilePurpose.ATTACHMENT;
    }

    @Override
    public void validate(UploadFileRequest request) {
        int fileByteSize = request.getFile().size();
        if (fileByteSize <= 0) {
            throw new AppCustomException(ErrorCode.ATTACHMENT_IS_EMPTY);
        }

        int maxByteSize = 20 * 1024 * 1024;
        if (fileByteSize > maxByteSize) {
            throw new AppCustomException(ErrorCode.ATTACHMENT_FAIL_TOO_LARGE);
        }
    }

    @Override
    public Path resolveDirectory(Path rootDirectory, UploadFileRequest request) {
        String ownerId = sanitizePathSegment(request.getOwnerId());
        return rootDirectory.resolve("attachments").resolve(ownerId);
    }

    private static String sanitizePathSegment(String input) {
        if (input == null || input.isBlank()) {
            return "unknown";
        }
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
