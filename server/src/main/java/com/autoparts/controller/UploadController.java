package com.autoparts.controller;

import com.autoparts.common.BusinessException;
import com.autoparts.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    @Value("${upload.path:uploads/images}")
    private String uploadPath;

    @Value("${upload.url-prefix:/uploads/images}")
    private String urlPrefix;

    /**
     * 上传图片
     * POST /api/v1/upload/image
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // 校验文件
        if (file.isEmpty()) {
            throw new BusinessException(400, "请选择文件");
        }

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!".jpg".equals(suffix) && !".jpeg".equals(suffix) && !".png".equals(suffix)
                && !".gif".equals(suffix) && !".webp".equals(suffix)) {
            throw new BusinessException(400, "仅支持 jpg/png/gif/webp 格式");
        }

        // 校验文件大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过 5MB");
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 保存文件
        try {
            String basePath = System.getProperty("user.dir");
            File dir = new File(basePath, uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, fileName);
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败: " + e.getMessage());
        }

        // 返回可访问的 URL
        String url = urlPrefix + "/" + fileName;
        Map<String, String> data = new LinkedHashMap<>();
        data.put("url", url);
        return Result.success(data);
    }
}
