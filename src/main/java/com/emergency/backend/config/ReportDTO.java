// ReportDTO.java
package com.emergency.backend.config;

import java.util.List;

public class ReportDTO {
    private Long id;
    private String description;
    private String address;
    private String type;
    private UserDTO user;
    private List<ReportMediaDTO> media;

    public ReportDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public List<ReportMediaDTO> getMedia() { return media; }
    public void setMedia(List<ReportMediaDTO> media) { this.media = media; }
}