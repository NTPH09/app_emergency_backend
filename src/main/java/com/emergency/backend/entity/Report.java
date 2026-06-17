package com.emergency.backend.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String address;
    private String type;
    private double latitude;
    private double longitude;
    private String status;
    private String cancelReason;

    private java.time.LocalDateTime createdAt;

    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user2) {
        this.user = user2;
    }


    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "report"})
    private List<ReportMedia> media;
    

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<ReportMedia> getMedia() {
        return media;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getCancelReason() {
        return cancelReason;
    }



    public void setId(Long id) {
        this.id = id;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setMedia(List<ReportMedia> media) {
        this.media = media;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

}
