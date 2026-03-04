package com.august.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.august.sharesecurity", "com.august.sharecore"})
@EnableElasticsearchRepositories(basePackages = "com.august.comment.repository.elastic")
@EnableMongoRepositories(basePackages = "com.august.comment.repository.jpa")
@EnableMongoAuditing
public class CommentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommentApplication.class, args);
    }

}
