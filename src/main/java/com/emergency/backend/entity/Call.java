package com.emergency.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.emergency.backend.config.CallStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "calls")
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 FIX: เปลี่ยนจาก String → User relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User receiver;

    @Column(name = "channel_name")
    private String channelName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Report report;

    @Enumerated(EnumType.STRING)
    private CallStatus status;

    private LocalDateTime createdAt;

    public Call() {
        this.status = CallStatus.CALLING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public User getCaller() { return caller; }
    public void setCaller(User caller) { this.caller = caller; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public CallStatus getStatus() { return status; }
    public void setStatus(CallStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }
}