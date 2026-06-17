package com.emergency.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.emergency.backend.entity.Report;
import com.emergency.backend.entity.ReportMedia;
import com.emergency.backend.entity.User;
import com.emergency.backend.repository.ReportRepository;
import com.emergency.backend.repository.ReportMediaRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMediaRepository reportMediaRepository;

    public ReportService(ReportRepository reportRepository,
                         ReportMediaRepository reportMediaRepository) {
        this.reportRepository = reportRepository;
        this.reportMediaRepository = reportMediaRepository;
    }

    /// CREATE REPORT
    public Report createReport(Map<String, Object> body, User user) {
        Report report = new Report();
        report.setUser(user);
        report.setType((String) body.get("type"));
        report.setDescription((String) body.get("description"));
        report.setAddress((String) body.get("address"));
        report.setLatitude(body.get("lat") != null 
            ? Double.valueOf(body.get("lat").toString()) 
            : 0.0);

        report.setLongitude(body.get("lng") != null 
            ? Double.valueOf(body.get("lng").toString()) 
            : 0.0);
        report.setCreatedAt(java.time.LocalDateTime.now());
        report.setStatus("ยืนยันการแจ้งเหตุ");

        reportRepository.save(report);

        // 🔥 cast แบบปลอดภัย
        Object mediaObj = body.get("media");
        List<Map<String, Object>> mediaList = new ArrayList<>();
        if (mediaObj instanceof List<?>) {
            for (Object item : (List<?>) mediaObj) {
                if (item instanceof Map<?, ?>) {
                    mediaList.add((Map<String, Object>) item);
                }
            }
        }

        List<ReportMedia> mediaEntities = new ArrayList<>();
        for (Map<String, Object> m : mediaList) {
            ReportMedia media = new ReportMedia();
            media.setUrl((String) m.get("url"));
            media.setType((String) m.get("type"));
            media.setReport(report);

            mediaEntities.add(media);
        }

        if (!mediaEntities.isEmpty()) {
            reportMediaRepository.saveAll(mediaEntities); // save ทีเดียว
        }

        report.setMedia(mediaEntities); // สำคัญ: attach media

        return report;
    }

    /// GET ALL REPORTS
    public List<Report> getAllReports() {
        return reportRepository.findAllWithMedia();
    }

    /// GET BY USER
    public List<Report> getReportsByUser(User user) {
        return reportRepository.findByUser(user);
    }

    /// GET BY ID
    public Report getReportById(Long id) {
        return reportRepository.findById(id).orElse(null);
    }

    /// UPDATE REPORT
    public Report updateReport(Long id, Report report) {
        Report existingReport = reportRepository.findById(id).orElse(null);
        if (existingReport != null) {
            existingReport.setDescription(report.getDescription());
            existingReport.setAddress(report.getAddress());
            existingReport.setLatitude(report.getLatitude());
            existingReport.setLongitude(report.getLongitude());
            existingReport.setType(report.getType());
            existingReport.setStatus(report.getStatus());

            // 🔥 update media
            if (existingReport.getMedia() != null) {
                existingReport.getMedia().clear();
            }
            if (report.getMedia() != null) {
                report.getMedia().forEach(m -> m.setReport(existingReport));
                existingReport.getMedia().addAll(report.getMedia());
            }

            return reportRepository.save(existingReport);
        }
        return null;
    }

    /// DELETE REPORT
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    /// UPDATE STATUS
    public Report updateStatus(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        String current = report.getStatus();

        // ❌ กันจบแล้ว
        if ("เสร็จสิ้น".equals(current) || "ยกเลิกการแจ้งเหตุ".equals(current)) {
            return report;
        }

        switch (current) {
            case "ยืนยันการแจ้งเหตุ" -> report.setStatus("กำลังตรวจสอบ");
            case "กำลังตรวจสอบ" -> report.setStatus("กำลังดำเนินการ");
            case "กำลังดำเนินการ" -> report.setStatus("เสร็จสิ้น");
        }

        return reportRepository.save(report);
    }

    /// CANCEL REPORT
    public Report cancelReport(Long id, String reason) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Report not found"));

        if ("เสร็จสิ้น".equals(report.getStatus())) {
            throw new RuntimeException("Cannot cancel completed report");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Reason is required");
        }

        report.setStatus("ยกเลิกการแจ้งเหตุ");
        report.setCancelReason(reason.trim());

        return reportRepository.save(report);
    }
}