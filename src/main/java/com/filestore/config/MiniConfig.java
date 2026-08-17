package com.filestore.config;


import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j

public class MiniConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        log.info("Initializing MinIO client at : {}",minioUrl);
        return MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey,secretKey)
                .build();
    }
}
