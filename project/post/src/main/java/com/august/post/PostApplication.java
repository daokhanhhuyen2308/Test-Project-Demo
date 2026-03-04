package com.august.post;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.august.post", "com.august.sharesecurity", "com.august.sharecore"})
@EnableScheduling
@EnableJpaRepositories(
        basePackages = "com.august.post.repository.jpa"
)
@EnableElasticsearchRepositories(
        basePackages = "com.august.post.repository.elastic"
)
public class PostApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostApplication.class, args);
    }

}