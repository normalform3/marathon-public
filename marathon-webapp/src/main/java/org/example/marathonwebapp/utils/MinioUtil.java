package org.example.marathonwebapp.utils;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;

import java.io.InputStream;
import java.util.UUID;

@Component
public class MinioUtil {

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    private final MinioClient minioClient;

    @Autowired
    public MinioUtil(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传文件到 MinIO 并返回文件的 URL
     * @param bucketName 存储桶名称
     * @param file 要上传的文件
     * @return 文件的访问 URL
     */
    public String uploadFile(String bucketName, MultipartFile file) throws Exception {
        // 检查存储桶是否存在，若不存在则创建
        if (!minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // 生成唯一的文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String uniqueObjectName = UUID.randomUUID().toString() + fileExtension;

        // 上传文件
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(uniqueObjectName)
                .stream(inputStream, file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        inputStream.close();

        // 构建普通的 URL
        return minioEndpoint + "/" + bucketName + "/" + uniqueObjectName;
    }
}