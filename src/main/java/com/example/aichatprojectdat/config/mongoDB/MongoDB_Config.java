package com.example.aichatprojectdat.config.mongoDB;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoDB_Config {

    @Value("${mongo.db.user}")
    private String mongoUser;

    @Value("${mongo.db.password}")
    private String mongoPassword;

    @Bean
    public MongoClient mongoClient() {
        String connectionString = String.format(
                "mongodb+srv://%s:%s@intellichat-mongodb.kxnrayx.mongodb.net/?retryWrites=true&w=majority",
                mongoUser, mongoPassword);

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        return MongoClients.create(settings);
    }
}
