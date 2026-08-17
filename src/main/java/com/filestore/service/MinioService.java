package com.filestore.service;


import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static com.google.common.io.Files.getFileExtension;

@Service
@RequiredArgsConstructor
@Slf4j

public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // Upload file to minio

    public String uploadFile(MultipartFile file) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException{

        // Ensure bucket exists
        ensureBucketExists();

        // Generate unique object name

        String objectName = generateObjectName(file.getOriginalFilename());
        log.info("Uploading file to MinIo {}",objectName);

        // Upload to MinIO
        minioClient.putObject(
                PutObjectArgs.builder().
                        bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(),file.getSize(),-1)
                        .contentType(file.getContentType())
                        .build()
        );
        log.info("File uploaded successfully: {}",objectName);
        return objectName;

    }

    public InputStream downloadFile(String objectName) throws MinioException,
            IOException,NoSuchAlgorithmException,InvalidKeyException {
        log.info("Downloading file from MiniIO : {} ", objectName);

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    // Delete file from MINIO

    public void deleteFile(String objectName)
            throws MinioException, IOException,
            NoSuchAlgorithmException,
            InvalidKeyException {

        log.info("Deleting file from MinIO: {}", objectName);

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );

        log.info("File deleted from MinIO: {}", objectName);
    }

    // HELPER METHODS

    private void ensureBucketExists () throws MinioException,
            IOException,NoSuchAlgorithmException,InvalidKeyException {

        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
        if (!exists) {
            log.info("Creating bucket: {}",bucketName);
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        }
    }

    private String generateObjectName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public String getBucketName() {
        return bucketName;
    }
}

