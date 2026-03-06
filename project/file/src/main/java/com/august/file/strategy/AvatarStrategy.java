package com.august.file.strategy;

import com.august.protocol.file.FilePurpose;
import com.august.protocol.file.UploadFileRequest;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class AvatarStrategy implements FileStrategy{
    @Override
    public FilePurpose getPurpose() {
        return FilePurpose.AVATAR;
    }

    @Override
    public void validate(UploadFileRequest request) {
        String contentType = request.getContentType();

        if (contentType.isBlank() || !contentType.startsWith("image/")) {
            throw new AppCustomException(ErrorCode.AVATAR_FAIL_CONTENT_TYPE);
        }

        int fileByteSize = request.getFile().size();
        int maxByteSize = 2 * 1024 * 1024;
        if (fileByteSize > maxByteSize) {
            throw new AppCustomException(ErrorCode.AVATAR_FAIL_TOO_LARGE);
        }
    }

    @Override
    public Path resolveDirectory(Path rootDirectory, UploadFileRequest request) {
        String ownerId = sanitizePathSegment(request.getOwnerId());
        return rootDirectory.resolve("avatars").resolve(ownerId);
    }

    private static String sanitizePathSegment(String input) {
        if (input == null || input.isBlank()) {
            return "unknown";
        }
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
