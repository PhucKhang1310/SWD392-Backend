package swd392.backend.domain.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String uploadFile(MultipartFile file, String folder) throws Exception;

    String getFileUrl(String fileName, String folder);

    InputStream getFile(String objectName, String folder) throws Exception;

    void deleteFile(String fileName, String folder) throws Exception;
}
