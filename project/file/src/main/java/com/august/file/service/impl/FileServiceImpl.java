package com.august.file.service.impl;

import com.august.file.dto.FileDownloadDTO;
import com.august.file.dto.FileResponse;
import com.august.file.entity.FileEntity;
import com.august.file.mapper.FileMapper;
import com.august.file.mapper.FilePurposeConverter;
import com.august.file.repository.FileRepository;
import com.august.file.service.FileService;
import com.august.file.strategy.FileStrategy;
import com.august.file.strategy.FileStrategyContext;
import com.august.protocol.file.FilePurpose;
import com.august.protocol.file.UploadFileRequest;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

    @Value("${app.upload.directory}")
    private String uploadDictionary;
    private final FileStrategyContext fileStrategyContext;

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Transactional
    @Override
    public FileResponse uploadFile(MultipartFile file){

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

            FileEntity entity = getFileEntity(file, rootLocation, uniqueFileName);

            fileRepository.save(entity);

            return fileMapper.mapToResponse(entity);

        } catch (Exception e) {
            throw new AppCustomException(ErrorCode.UPLOAD_FAILED);
        }

    }

    @Override
    public FileDownloadDTO extractFile(String fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new AppCustomException(ErrorCode.FILE_NOT_FOUND, fileId));

        try{
            // uploadDictionary = "uploads"
            Path rootLocation = Paths.get(uploadDictionary);
            Path filePath = rootLocation.resolve(file.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()){
                throw new AppCustomException(ErrorCode.UPLOAD_FAILED);
            }

            return new FileDownloadDTO(
              resource,
                    file.getContentType(),
                    file.getFileName(),
                    file.isViewable()

            );

        } catch (MalformedURLException e) {
            log.error("Lỗi đường dẫn file: {}", e.getMessage());
            throw new AppCustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @Override
    public FileResponse uploadFileFromGrpc(UploadFileRequest fileRequest) {

        FilePurpose purpose = fileRequest.getPurpose();

        if (purpose == FilePurpose.UNKNOWN) {
            throw new AppCustomException(ErrorCode.INVALID_FILE_PURPOSE);
        }

        //get strategy based on purpose, for example: purpose AVATAR -> get AvatarStrategy
        FileStrategy strategy = fileStrategyContext.getStrategy(purpose);
        strategy.validate(fileRequest);

        Path rootLocation = Paths.get(uploadDictionary);
        Path targetDirectoryPath = strategy.resolveDirectory(rootLocation, fileRequest);

        try {
            Files.createDirectories(targetDirectoryPath);
        } catch (Exception exception) {
            throw new RuntimeException("Cannot create upload directory", exception);
        }

        String originalFileName = fileRequest.getFileName();
        String extension = "";
        if (originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID() + extension;
        Path storedFilePath = targetDirectoryPath.resolve(uniqueFileName).normalize();

        try {
            //Ghi file vật lý vào ổ đĩa
            Files.write(storedFilePath, fileRequest.getFile().toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException("Cannot write file to disk", exception);
        }

        String contentType = fileRequest.getContentType();

        boolean isViewable = contentType.startsWith("image/") || contentType.equals("application/pdf");

        String filePath = rootLocation.relativize(storedFilePath).toString().replace('\\', '/');

        FileEntity entity = fileMapper.mapToFileEntity(fileRequest);
        entity.setSize(fileRequest.getFile().size());
        entity.setFilePath(filePath);
        entity.setFileName(uniqueFileName);
        entity.setContentType(contentType);
        entity.setViewable(isViewable);
        entity.setPurpose(FilePurposeConverter.toEntity(fileRequest.getPurpose()));

        System.out.println("Running before saving into file database");

        fileRepository.save(entity);

        FileEntity saved = fileRepository.save(entity);
        if (saved.getId() != null) {
            System.out.println(">>> TRỜI ƠI, NÓ LƯU ĐƯỢC RỒI! ID ĐÂY: " + saved.getId());
        } else {
            System.out.println(">>> LƯU THẤT BẠI RỒI HUYỀN ƠI, ID VẪN NULL!");
        }

        return fileMapper.mapToResponse(entity);
    }

    private static FileEntity getFileEntity(MultipartFile file, Path rootLocation,
                                            String uniqueFileName) {
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
        entity.setOwnerId("123");
        return entity;
    }

}
