package com.emergency.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.emergency.backend.config.CallDTO;
import com.emergency.backend.config.CallStatus;
import com.emergency.backend.entity.Call;
import com.emergency.backend.entity.Report;
import com.emergency.backend.entity.User;
import com.emergency.backend.repository.CallRepository;
import com.emergency.backend.repository.ReportRepository;

@Service
public class CallService {

    private static final Logger logger = LoggerFactory.getLogger(CallService.class);

    private final CallRepository callRepository;
    private final ReportRepository reportRepository;
    private final WebSocketService webSocketService;
    private final UserService userService;

    public CallService(CallRepository callRepository,
                       ReportRepository reportRepository,
                       WebSocketService webSocketService,
                       UserService userService) {
        this.callRepository = callRepository;
        this.reportRepository = reportRepository;
        this.webSocketService = webSocketService;
        this.userService = userService;
    }

    // =========================
    // CREATE CALL
    // =========================
    public Call createCall(Call call) {

        call.setCreatedAt(LocalDateTime.now());
        call.setStatus(CallStatus.CALLING);
        call.setChannelName("call_" + System.currentTimeMillis());

        return callRepository.save(call);
    }

    // =========================
    // CREATE CALL FROM REPORT
    // =========================
    public Call createCallByReport(Long reportId, String callerId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        // 🔥 แปลง callerId → User
        User callerUser = userService.findById(Long.parseLong(callerId));

        // 🔥 receiver มาจาก report
        User receiverUser = report.getUser();

        Call call = new Call();
        call.setCaller(callerUser);
        call.setReceiver(receiverUser);
        call.setReport(report);

        Call saved = createCall(call);

        // 🔥 ยิง WebSocket ไปหา receiver (ใช้ id)
        webSocketService.sendCallToUser(
                String.valueOf(receiverUser.getId()),
                new CallDTO(saved)
        );

        logger.info("📞 Call created id={}, caller={}, receiver={}",
                saved.getId(),
                callerUser.getFirstname(),
                receiverUser.getFirstname()
        );

        return saved;
    }

    // =========================
    // GET ALL
    // =========================
    public List<Call> getAllCalls() {
        return callRepository.findAll();
    }

    public Call getCallById(Long id) {
        return callRepository.findById(id).orElse(null);
    }

    // 🔥 FIX: ใช้ _Id เพราะ receiver เป็น User แล้ว
    public List<Call> getIncomingCalls(String receiver) {
        return callRepository.findByReceiver_IdAndStatus(
                Long.parseLong(receiver),
                CallStatus.CALLING
        );
    }

    // =========================
    // ACTIONS
    // =========================

    public Call acceptCall(Long id) {
        Call call = callRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        call.setStatus(CallStatus.ACCEPTED);
        return callRepository.save(call);
    }

    public Call rejectCall(Long id) {
        return updateCallStatus(id, CallStatus.REJECTED);
    }

    public Call endCall(Long id) {
        return updateCallStatus(id, CallStatus.ENDED);
    }

    private Call updateCallStatus(Long id, CallStatus status) {
        return callRepository.findById(id)
                .map(call -> {
                    call.setStatus(status);
                    return callRepository.save(call);
                })
                .orElse(null);
    }
}