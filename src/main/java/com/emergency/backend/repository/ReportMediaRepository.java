package com.emergency.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.emergency.backend.entity.ReportMedia;

public interface ReportMediaRepository extends JpaRepository<ReportMedia, Long> {
}