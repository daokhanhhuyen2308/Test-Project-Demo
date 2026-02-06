package com.august.file.service.impl;

import com.august.file.dto.requests.EmailDetailRequest;
import com.august.file.dto.requests.EmailTaskDTO;
import com.august.file.dto.requests.FileDownloadDTO;
import com.august.file.dto.responses.FileResponse;
import com.august.file.entity.FileEntity;
import com.august.file.enums.StatusSendEmail;
import com.august.file.mapper.FileMapper;
import com.august.file.repository.FileRepository;
import com.august.file.service.FileService;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final EmailTaskRedisService emailTaskRedisService;
    private final FileMapper fileMapper;

    @Value("${app.upload.directory}")
    private String uploadDictionary;

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Transactional
    @Override
    public FileResponse uploadFile(MultipartFile file, String recipient) {
        if (file.isEmpty()){
            logger.warn("File is empty. Please check method again!");
            throw new AppCustomException(ErrorCode.PAYLOAD_TOO_LARGE);
        }

        try{
            Path rootLocation = Paths.get(uploadDictionary);
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID() + extension;

            FileEntity entity = getFileEntity(file, rootLocation, uniqueFileName, recipient);

            fileRepository.save(entity);

            EmailDetailRequest request = EmailDetailRequest.builder()
                    .recipient(recipient)
                    .subject("Upload File/Image")
                    .build();

            EmailTaskDTO emailTaskDTO = EmailTaskDTO.builder()
                    .status(StatusSendEmail.EMAIL_PENDING)
                    .retryCount(0)
                    .maxRetryCount(3)
                    .request(request)
                    .build();

            emailTaskRedisService.save(emailTaskDTO);

            return fileMapper.mapToResponse(entity);

        } catch (Exception e) {
            throw new AppCustomException(ErrorCode.UPLOAD_FAILED);
        }

    }

    @Override
    public FileDownloadDTO extractFile(String fileId) {
        FileEntity file = fileRepository.findFileEntityById(fileId);

        try{
            Path path = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() && !resource.isReadable()){
                throw new AppCustomException(ErrorCode.UPLOAD_FAILED);
            }

            return new FileDownloadDTO(
              resource,
                    file.getContentType(),
                    file.getFileName(),
                    file.isViewable()

            );

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileEntity getFileEntity(MultipartFile file, Path rootLocation, String uniqueFileName, String recipient) {
        Path destinationFile = rootLocation.resolve(uniqueFileName);

        String contentType = file.getContentType();

        boolean isViewable = contentType != null &&
                (contentType.startsWith("image/") || contentType.equals("application/pdf"));

        FileEntity entity = new FileEntity();
        entity.setSize(file.getSize());
        entity.setFilePath(destinationFile.toString());
        entity.setFileName(uniqueFileName);
        entity.setContentType(file.getContentType());
        entity.setViewable(isViewable);
        entity.setOwnerId(recipient);
        return entity;
    }

}
