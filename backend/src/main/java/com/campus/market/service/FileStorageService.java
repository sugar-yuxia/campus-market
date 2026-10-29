package com.campus.market.service;

import com.campus.market.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 本地图片存储：上传文件保存到 app.upload-dir，通过 /uploads/** 静态资源访问。
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024L;

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) throws IOException {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException("只支持上传图片文件");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BizException("只支持 jpg、jpeg、png、gif、webp 格式");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = root.resolve(filename);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new BizException(500, "图片保存失败，请稍后重试");
        }
        return "/uploads/" + filename;
    }
}
