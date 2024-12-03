package com.rishiraj.s3_file_upload.controller;

import com.rishiraj.s3_file_upload.exceptions.FileUploadException;
import com.rishiraj.s3_file_upload.implementation.S3FileUploadImpl;
import com.rishiraj.s3_file_upload.service.S3FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/s3")
@CrossOrigin("http://localhost:5173")
public class S3FileUploadController {

    @Autowired
    private S3FileUploadService fileUploadService;

    @GetMapping
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Successful", HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {

        try {
            String uploadedFileName = fileUploadService.uploadFile(file);
            return new ResponseEntity<>(uploadedFileName, HttpStatus.OK);

        } catch (
                FileUploadException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping("/all-files")
    public ResponseEntity<List<String>> getAllFiles() {
        List<String> allFiles = fileUploadService.getAllFiles();
        return new ResponseEntity<>(allFiles, HttpStatus.OK);
    }

    @GetMapping("/{filename}")
    public ResponseEntity<String> getFileByName(@PathVariable("filename") String filename) {
        String preSignedFileUrl = fileUploadService.getFileUrlByName(filename);
        return new ResponseEntity<>(preSignedFileUrl, HttpStatus.OK);
    }
}
