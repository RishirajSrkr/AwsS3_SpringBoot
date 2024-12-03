package com.rishiraj.s3_file_upload.implementation;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.rishiraj.s3_file_upload.exceptions.FileUploadException;
import com.rishiraj.s3_file_upload.service.S3FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class S3FileUploadImpl implements S3FileUploadService {

    @Autowired
    private AmazonS3 client;

    @Value("${app.s3.bucket}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file) throws FileUploadException {

        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is null.");
        }

        //create a unique name for the file
        String actualFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + actualFileName.substring(actualFileName.lastIndexOf('.'));


        //upload the image to s3
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            PutObjectResult putObjectResult = client.putObject(new PutObjectRequest(bucketName, uniqueFileName, file.getInputStream(), metadata));
            return preSignedUrl(uniqueFileName);
        } catch (
                IOException e) {
            throw new FileUploadException("Error in uploading image : " + e.getMessage());
        }
    }


    @Override
    public List<String> getAllFiles() {

        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request();
        listObjectsV2Request.withBucketName(bucketName);

        ListObjectsV2Result listObjectsV2Result = client.listObjectsV2(listObjectsV2Request);

        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();

        List<String> listOfFiles_PreSignedUrls = objectSummaries.stream()
                .map(summary -> preSignedUrl(summary.getKey()))
                .collect(Collectors.toList());

        return listOfFiles_PreSignedUrls;
    }



    @Override
    public String preSignedUrl(String fileName) {

        //current date
        Date expirationData = new Date();
        //get time gives milliseconds
        long time = expirationData.getTime();
        int hour = 2;
        time = time + hour * 60 * 60 * 1000;
        expirationData.setTime(time);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, fileName);
        generatePresignedUrlRequest.withMethod(HttpMethod.GET);
        generatePresignedUrlRequest.withExpiration(expirationData);

        URL preSignedUrl = client.generatePresignedUrl(generatePresignedUrlRequest);
        return preSignedUrl.toString();
    }


    @Override
    public String getFileUrlByName(String fileName) {
        S3Object object = client.getObject(bucketName, fileName);
        //getKey gives the raw file name, the file name stored in s3 bucket
        return preSignedUrl(object.getKey());
    }
}
