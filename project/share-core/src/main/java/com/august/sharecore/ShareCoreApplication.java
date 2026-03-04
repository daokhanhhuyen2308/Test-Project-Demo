package com.august.sharecore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.august"})
public class ShareCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareCoreApplication.class, args);
    }

}
