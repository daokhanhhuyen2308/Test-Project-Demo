package com.example.demo.service.impl;

import com.example.demo.dto.EmailDetail;
import com.example.demo.dto.FileDownloadDTO;
import com.example.demo.dto.FileResponse;
import com.example.demo.entity.FileEntity;
import com.example.demo.mapper.FileMapper;
import com.example.demo.repository.FileRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.FileService;
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
    private final EmailService emailService;

    @Value("${app.upload.directory}")
    private String uploadDictionary;

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Transactional
    @Override
    public FileResponse uploadFile(MultipartFile file, String recipient) {
        if (file.isEmpty()){
            logger.warn("File is empty. Please check method again!");
            throw new RuntimeException("File is not empty. Please choose any files!");
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

            String htmlContent = """
                <h3>Chúc mừng!</h3>
                <p>Ảnh của bạn đã được lưu trữ thành công trên hệ thống.</p>
                <p>Cảm ơn bạn đã sử dụng dịch vụ!</p>
                """;

                    EmailDetail emailDetail = EmailDetail.builder()
                    .msgBody(htmlContent)
                    .recipient(recipient)
                            .subject("Upload file")
                    .build();

            emailService.sendEmail(emailDetail);

            return FileMapper.mapToFileResponse(entity);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public FileDownloadDTO extractFile(String fileId) {
        FileEntity file = fileRepository.findFileEntityById(fileId);

        try{
            Path path = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() && !resource.isReadable()){
                throw new RuntimeException("File is error!");
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

    private static FileEntity getFileEntity(MultipartFile file, Path rootLocation, String uniqueFileName) {
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
        return entity;
    }

}
