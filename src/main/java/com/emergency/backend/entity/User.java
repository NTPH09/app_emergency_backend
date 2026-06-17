package com.emergency.backend.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;
    private String lastname;
    private String role;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Column(unique = true, nullable = true)
    private String email;

    @Column(unique = true, nullable = true)
    private String phone;

    @Column(nullable = false)
    private String avatar;

    private String password;
    private String photoUrl;
    private Boolean useAvatar = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<UserProvider> providers = new ArrayList<>();

    // ------------------- Getters -------------------
    public Long getId() { return id; }
    public String getFirstname() { return firstname; }
    public String getLastname() { return lastname; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getAvatar() { return avatar; }
    public String getPhotoUrl() { return photoUrl; }
    public Boolean getUseAvatar() { return useAvatar; }
    public List<UserProvider> getProviders() { return providers; }

    // ------------------- Setters -------------------
    public void setId(Long id) { this.id = id; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setUseAvatar(Boolean useAvatar) { this.useAvatar = useAvatar; }
    public void setProviders(List<UserProvider> providers) { this.providers = providers; }
}