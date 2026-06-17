package com.emergency.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.emergency.backend.config.CallStatus;
import com.emergency.backend.entity.Call;

public interface CallRepository extends JpaRepository<Call, Long> {
    List<Call> findByReceiver_IdAndStatus(Long receiverId, CallStatus status);
    boolean existsByReceiver_IdAndStatusIn(Long receiverId, List<CallStatus> statuses);
}