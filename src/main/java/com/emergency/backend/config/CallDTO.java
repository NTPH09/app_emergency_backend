package com.emergency.backend.config;

import com.emergency.backend.entity.Call;

public class CallDTO {

    private Long id;

    private Long callerId;
    private String callerName;

    private Long receiverId;
    private String receiverName;

    private String status;
    private String channelName;
    private String createdAt;

    public CallDTO(Call call) {
        this.id = call.getId();

        if (call.getCaller() != null) {
            this.callerId = call.getCaller().getId();
            this.callerName = call.getCaller().getFirstname();
        }

        if (call.getReceiver() != null) {
            this.receiverId = call.getReceiver().getId();
            this.receiverName = call.getReceiver().getFirstname();
        }

        this.status = call.getStatus().name();
        this.channelName = call.getChannelName();
        this.createdAt = call.getCreatedAt().toString();
    }

    public Long getId() { return id; }

    public Long getCallerId() { return callerId; }
    public String getCallerName() { return callerName; }

    public Long getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName; }

    public String getStatus() { return status; }
    public String getChannelName() { return channelName; }

    public String getCreatedAt() { return createdAt; }
}