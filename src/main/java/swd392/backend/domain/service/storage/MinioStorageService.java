package swd392.backend.domain.service.storage;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.public-url:#{null}}")
    private String publicUrl;

    @Override
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        // Ensure bucket exists
        ensureBucketExists();

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String fileName = UUID.randomUUID().toString() + extension;
        String objectName = folder.isEmpty() ? fileName : folder + "/" + fileName;

        // Upload file
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        }

        log.info("File uploaded successfully: {}", objectName);
        return objectName;
    }

    @Override
    public String getFileUrl(String objectName, String folder) {
        if (!folder.isEmpty() && !objectName.startsWith(folder + "/")) {
            objectName = folder + "/" + objectName;
        }

        try {
            // Generate presigned URL valid for 7 days
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build());

            // If public URL is configured, replace endpoint with public URL
            if (publicUrl != null && !publicUrl.isEmpty()) {
                url = url.replace(endpoint, publicUrl);
            }

            return url;
        } catch (Exception e) {
            log.error("Error generating file URL for: {}", objectName, e);
            return null;
        }
    }

    @Override
    public InputStream getFile(String objectName, String folder) throws Exception {
        if (!folder.isEmpty() && !objectName.startsWith(folder + "/")) {
            objectName = folder + "/" + objectName;
        }

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            log.info("File retrieved successfully: {}", objectName);
            return stream;
        } catch (Exception e) {
            log.error("Error getting file from MinIO: {}", objectName, e);
            throw new RuntimeException("Error getting file from MinIO: " + objectName, e);
        }
    }

    @Override
    public void deleteFile(String objectName, String folder) throws Exception {
        if (!folder.isEmpty() && !objectName.startsWith(folder + "/")) {
            objectName = folder + "/" + objectName;
        }

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());

        log.info("File deleted successfully: {}", objectName);
    }

    private void ensureBucketExists() throws Exception {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());

        if (!found) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
            log.info("Bucket created: {}", bucketName);
        }
    }
}
