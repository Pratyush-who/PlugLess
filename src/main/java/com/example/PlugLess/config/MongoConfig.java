package com.example.PlugLess.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        log.info(">>> Connecting to MongoDB: {}", mongoUri.replaceAll(":([^@]+)@", ":<password>@"));
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        ConnectionString cs = new ConnectionString(mongoUri);
        String database = cs.getDatabase();
        if (database == null || database.isBlank()) {
            database = "plugless";
        }
        log.info(">>> Using MongoDB database: {}", database);
        return new MongoTemplate(mongoClient(), database);
    }
}
