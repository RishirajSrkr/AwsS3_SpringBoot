package com.rishiraj.s3_file_upload.service;

import com.rishiraj.s3_file_upload.exceptions.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3FileUploadService {

    String uploadFile(MultipartFile file) throws FileUploadException;

    List<String> getAllFiles();

    String preSignedUrl(String fileName);

    String getFileUrlByName(String fileName);
}
