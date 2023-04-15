package ru.timur.project.Hubr.util.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.timur.project.Hubr.models.User;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


@Component
public class FileManipulation {
    @Value("${upload.path}")
    private String uploadPath;

    public String saveToFile(String path, MultipartFile file) {

        if (file != null && !file.getOriginalFilename().isEmpty()) {
            File upDir = new File(uploadPath);

            if (!upDir.exists()) {
                upDir.mkdir();
            }

            String uuidFile = UUID.randomUUID().toString();
            String result = uuidFile + file.getOriginalFilename();

            try {
                file.transferTo(new File(uploadPath + "/" + path + "/" + result));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return path + "/" + result;
        }
        return null;
    }

    public String uploadFile(String path, String oldFileName, MultipartFile file) {
        if (oldFileName != null &&!oldFileName.equals("userImg/defaultAvatar.png")) {
            File deleteFile = new File(uploadPath + "/" + oldFileName);
            deleteFile.delete();
        }
        return saveToFile(path, file);

    }

    public void deleteFile(String fileName) {
        File deleteFile = new File(uploadPath + "/" + fileName);
        deleteFile.delete();
    }

}
