package com.emergency.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.emergency.backend.config.CallDTO;
import com.emergency.backend.config.JwtUtil;
import com.emergency.backend.entity.Call;
import com.emergency.backend.entity.User;
import com.emergency.backend.service.CallService;
import com.emergency.backend.service.UserService;

@RestController
@RequestMapping("/call")
public class CallController {

    private final CallService callService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public CallController(CallService callService, JwtUtil jwtUtil, UserService userService) {
        this.callService = callService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping
    public CallDTO createCall(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Call call
    ) {
        String userId = jwtUtil.extractUserId(authHeader.replace("Bearer ", ""));
        User caller = userService.findById(Long.parseLong(userId));

        call.setCaller(caller);

        return new CallDTO(callService.createCall(call));
    }

    @PostMapping("/report/{reportId}")
    public CallDTO callUserByReport(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("reportId") Long reportId
    ) {
        User user = userService.getUserFromToken(authHeader);

        if (!"ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return new CallDTO(
                callService.createCallByReport(reportId, String.valueOf(user.getId()))
        );
    }

    @GetMapping
    public List<CallDTO> getAllCalls() {
        return callService.getAllCalls()
                .stream()
                .map(CallDTO::new)
                .toList();
    }

    @GetMapping("/{id}")
    public CallDTO getCallById(@PathVariable Long id) {
        return new CallDTO(callService.getCallById(id));
    }

    @GetMapping("/incoming/{receiver}")
    public List<CallDTO> getIncomingCalls(
            @PathVariable String receiver,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = userService.getUserFromToken(authHeader);

        if (!String.valueOf(user.getId()).equals(receiver)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return callService.getIncomingCalls(receiver)
                .stream()
                .map(CallDTO::new)
                .toList();
    }

    @PutMapping("/{id}/accept")
    public CallDTO acceptCall(@PathVariable("id") Long id) {
        return new CallDTO(callService.acceptCall(id));
    }

    @PutMapping("/{id}/reject")
    public CallDTO rejectCall(@PathVariable("id") Long id) {
        return new CallDTO(callService.rejectCall(id));
    }

    @PutMapping("/{id}/end")
    public CallDTO endCall(@PathVariable("id") Long id) {
        return new CallDTO(callService.endCall(id));
    }
}