package com.user.register.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final String UPLOAD_DIR = "uploads/profile/"; // local storage (can be S3 later)

    @PostMapping("/upload")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Validate file size
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File size exceeds 5MB");
            }

            // 2. Validate file type
            String contentType = file.getContentType();
            if (!("image/jpeg".equals(contentType) || "image/png".equals(contentType) || "image/webp".equals(contentType))) {
                return ResponseEntity.badRequest().body("Only JPG, PNG, WEBP allowed");
            }

            // 3. Resize image to 512x512
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, 512, 512, null);
            g.dispose();

            // 4. Save file with unique name
            String fileExtension = contentType.split("/")[1]; // jpg/png/webp
            String fileName = UUID.randomUUID().toString() + "." + fileExtension;
            File outputFile = new File(UPLOAD_DIR + fileName);
            outputFile.getParentFile().mkdirs(); // create directories if not exists
            ImageIO.write(resizedImage, fileExtension, outputFile);

            // 5. Return URL (for now local URL)
            String fileUrl = "/files/profile/" + fileName;
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image");
        }
    }
}
