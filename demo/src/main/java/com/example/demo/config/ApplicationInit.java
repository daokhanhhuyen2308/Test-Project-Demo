package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@org.jspecify.annotations.NullMarked
@Component
@Slf4j
public class ApplicationInit implements ApplicationRunner {

    @Value("${app.upload.directory}")
    private String storageLocation;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try{
            Path rootLocation = Paths.get(storageLocation);
            if (!Files.exists(rootLocation)){
                log.info("System has been being existed : {}", rootLocation.toAbsolutePath());
            }
            else {
                Files.createDirectories(rootLocation);
                log.info("System has been being saved: Created successfully {}", rootLocation.toAbsolutePath());
            }

            if (!Files.isWritable(rootLocation)){
                log.info("Access is available");
            }

        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }

    }
}
