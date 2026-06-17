package com.emergency.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class ReportMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private String type;

    @ManyToOne
    @JoinColumn(name = "report_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "report"})
    private Report report;

    // ✅ เพิ่ม getId() ที่หายไป
    public Long getId() { return id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }
}