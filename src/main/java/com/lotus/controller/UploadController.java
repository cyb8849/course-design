package com.lotus.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.lotus.common.ResultVO;
import com.lotus.config.OSSConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 文件上传控制器
 * 处理图片上传等文件上传请求
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final OSS ossClient;
    private final OSSConfig ossConfig;

    /**
     * 上传图片
     * @param file 图片文件
     * @return 图片访问URL
     */
    @PostMapping("/image")
    public ResultVO<String> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("开始处理图片上传请求");
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                log.error("文件为空");
                return ResultVO.error("文件不能为空");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.error("文件类型不支持: {}", contentType);
                return ResultVO.error("只支持图片文件");
            }

            // 检查文件大小（限制5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                log.error("文件大小超过限制: {}MB", file.getSize() / (1024 * 1024));
                return ResultVO.error("文件大小不能超过5MB");
            }

            // 检查OSS配置
            if (ossConfig.getEndpoint() == null || ossConfig.getAccessKeyId() == null || 
                ossConfig.getAccessKeySecret() == null || ossConfig.getBucketName() == null) {
                log.error("OSS配置不完整");
                return ResultVO.error("OSS配置不完整");
            }

            log.info("文件信息: 名称={}, 类型={}, 大小={}KB", 
                     file.getOriginalFilename(), contentType, file.getSize() / 1024);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = "images/" + UUID.randomUUID().toString() + fileExtension;

            log.info("准备上传到OSS: bucket={}, filename={}", ossConfig.getBucketName(), filename);

            // 上传到阿里云OSS
            try (InputStream inputStream = file.getInputStream()) {
                log.info("开始上传文件到OSS");
                ossClient.putObject(ossConfig.getBucketName(), filename, inputStream);
                log.info("文件上传到OSS成功");
            }

            // 生成访问URL
            String imageUrl = ossConfig.getDomain() + "/" + filename;

            log.info("图片上传成功: {}", imageUrl);
            return ResultVO.success("上传成功", imageUrl);

        } catch (OSSException e) {
            log.error("阿里云OSS上传失败: {}", e.getMessage());
            log.error("OSS错误详情: {}", e.getErrorMessage());
            return ResultVO.error("上传失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage());
            e.printStackTrace();
            return ResultVO.error("上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传过程中发生未知错误: {}", e.getMessage());
            e.printStackTrace();
            return ResultVO.error("上传失败: " + e.getMessage());
        }
    }
}