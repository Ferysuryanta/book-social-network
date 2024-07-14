package com.network.file;

import com.network.book.Book;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    @Value("${application.file.upload.photos-output-path}")
    private String fileUploadPath;
    public String saveFile(
           @NonNull MultipartFile sourceFile,
           @NonNull Integer userId) {
        final String fileUploadSubPath = "users" + separator + userId;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    public String uploadFile(
            @NonNull MultipartFile sourceFile,
            @NonNull String fileUploadSubPath) {
        var finalUploadPath = fileUploadPath + separator + fileUploadSubPath;
        var targetFolder = new File(finalUploadPath);
        if (!targetFolder.exists()) {
            var folderCreated = targetFolder.mkdirs();
            if (!folderCreated) {
                log.warn("Failed to created the target folder");
                return null;
            }
        }
        var fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        // ./upload/users/1/232423232.jpg
        var targetFilePath = finalUploadPath + separator + currentTimeMillis() + "." + fileExtension;
        var targetPath = Paths.get(targetFilePath);
        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File save to : " + targetFilePath);
            return targetFilePath;
        } catch (IOException e) {
            log.error("File was not saved", e);
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
