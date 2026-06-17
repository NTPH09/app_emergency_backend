package com.emergency.backend.entity;

public enum Provider {
    GOOGLE,
    FACEBOOK,
    LINE,
    LOCAL,
    PHONE;

    public static Provider fromFirebase(String firebaseProvider) {
        if (firebaseProvider == null) return null;

        return switch (firebaseProvider) {
            case "google.com" -> GOOGLE;
            case "facebook.com" -> FACEBOOK;
            default -> throw new RuntimeException("Unsupported provider: " + firebaseProvider);
        };
    }

    public String toValue() {
        return this.name().toLowerCase();
    }
}