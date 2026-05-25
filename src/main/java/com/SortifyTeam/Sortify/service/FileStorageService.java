package com.SortifyTeam.Sortify.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path basePath;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String storeFile(MultipartFile file, String subFolder) {
        try {
            Path targetDir = basePath;
            String prefix = "";
            if (subFolder != null && !subFolder.isBlank()) {
                targetDir = basePath.resolve(subFolder);
                prefix = subFolder + "/";
            }
            Files.createDirectories(targetDir);

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + ext;

            Path targetPath = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("[UPLOAD] File {} tersimpan sebagai {}", original, prefix + filename);
            return prefix + filename;
        } catch (IOException e) {
            throw new RuntimeException("Gagal menyimpan file: " + e.getMessage());
        }
    }
}
