package com.emergency.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "user_providers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "providerId"})
    }
)
public class UserProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "provider")
    @Convert(converter = ProviderConverter.class)  // ← เปลี่ยนบรรทัดนี้
    private Provider provider;

    private String providerId;
    private String photoUrl;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Provider getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public String getPhotoUrl() { return photoUrl; }
    public void setUser(User user) { this.user = user; }
    public void setProvider(Provider provider) { this.provider = provider; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}