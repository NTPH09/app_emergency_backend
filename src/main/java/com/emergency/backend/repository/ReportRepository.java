package com.emergency.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.emergency.backend.entity.Report;
import com.emergency.backend.entity.User;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUser(User user);

    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.media")
    List<Report> findAllWithMedia();
}