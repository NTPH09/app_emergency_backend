package com.emergency.backend.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.emergency.backend.entity.Report;
import com.emergency.backend.entity.User;
import com.emergency.backend.service.ReportService;
import com.emergency.backend.service.UserService;

@RestController
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    // ✅ ดึง base URL จาก application.properties
    @Value("${app.base-url}")
    private String baseUrl;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @PostMapping
    public Report createReport(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        User user = userService.getUserFromToken(authHeader);
        return reportService.createReport(body, user);
    }

    @GetMapping("/my")
    public List<Report> getMyReports(@RequestHeader("Authorization") String authHeader) {
        User user = userService.getUserFromToken(authHeader);
        return reportService.getReportsByUser(user);
    }

    @GetMapping
    public List<Report> getAllReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/{id}")
    public Report getReportById(@PathVariable("id") Long id) {
        return reportService.getReportById(id);
    }

    @PutMapping("/{id}")
    public Report updateReport(@PathVariable("id") Long id, @RequestBody Report report) {
        return reportService.updateReport(id, report);
    }

    @DeleteMapping("/{id}")
    public String deleteReport(@PathVariable("id") Long id) {
        reportService.deleteReport(id);
        return "Report deleted successfully";
    }

    @PostMapping("/upload")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            file.transferTo(new File(uploadDir + fileName));

            String originalName = file.getOriginalFilename();
            boolean isVideo = originalName != null && originalName.toLowerCase().endsWith(".mp4");

            // ✅ ใช้ baseUrl จาก properties แทน hardcode
            String url = isVideo
                    ? baseUrl + "/report/video/" + fileName
                    : baseUrl + "/uploads/" + fileName;

            return Map.of("url", url);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Upload failed");
        }
    }

    @GetMapping("/video/{filename}")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable("filename") String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) throws Exception {
        File file = new File("uploads/" + filename);
        Resource resource = new UrlResource(file.toURI());

        return ResponseEntity.ok()
                .header("Content-Type", "video/mp4")
                .body(resource);
    }

    @PutMapping("/{id}/status")
    public Report updateStatus(@PathVariable("id") Long id) {
        return reportService.updateStatus(id);
    }

    @PutMapping("/{id}/cancel")
    public Report cancelReport(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body
    ) {
        return reportService.cancelReport(id, body.get("reason"));
    }
}