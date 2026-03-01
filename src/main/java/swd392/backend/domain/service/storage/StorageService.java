package swd392.backend.domain.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String folder) throws Exception;

    String getFileUrl(String fileName, String folder);

    void deleteFile(String fileName, String folder) throws Exception;
}
